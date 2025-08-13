import argparse
import re
import pandas as pd
from datetime import datetime
import numpy as np
import logging

from warnings import simplefilter
simplefilter(action="ignore", category=pd.errors.PerformanceWarning)

# DEBUG=10, INFO=20, WARNING=30, ERROR=40, CRITICAL=50
LOGGING_LEVEL = 20

# Sensor modes that do not contain measurement data
NON_DATA_MODES = ["[PCO2 START ACQUISITION]", "[PCO2 STATUS]", "[PCO2 END ACQUISITION]"]

# Holds details of an acquisition as it is extracted from the file.
class Acquisition:
    def __init__(self):
        self.timestamp = None
        self.longitude = None
        self.latitude = None
        self.status = None

        self.data = dict()

    def is_complete(self):
        return self.timestamp is not None and self.status is not None

    def set_position(self, lon, lat):
        self.longitude = lon
        self.latitude = lat

    def add_values(self, mode, sensor, values):

        # Write the current values
        if len(values) > 0:
            column_name = mode
            if sensor is not None:
                column_name += f'_{sensor}'

            calc_values = np.array(values, dtype=np.float64)

            mean = np.mean(calc_values)
            stdev = -1 if sensor is None else np.std(calc_values)

            self.data[column_name] = [mean, stdev]
    
    def write(self, df, sequence):
        # If this acquisition is not complete, do nothing.
        if self.is_complete():
            df.at[sequence, 'Time'] = self.timestamp
            df.at[sequence, 'StatusCode'] = self.status

            if self.longitude is not None:
                df.at[sequence, 'Longitude'] = self.longitude
                df.at[sequence, 'Latitude'] = self.latitude

            for column, values in self.data.items():
                # If we have only one value, just store that
                # This is indicated by a stdev of -1
                if values[1] == -1:
                    df.at[sequence, column] = values[0]
                else:
                    df.at[sequence, f'{column}_mean'] = values[0]
                    df.at[sequence, f'{column}_sd'] = values[1]
            

def write_file_header(outfile, lines):
    # Write the dataset ID as the file header.
    # Find the first PCO2 START ACQUISITION and take the line after that.
    current_line = 0
    acquisition_found = False
    while not acquisition_found:
        if current_line >= len(lines):
            print("ERROR: Could not find PCO2 START ACQUISITION line. Is this a GENx file?")
            exit(1)
        elif lines[current_line] == "[PCO2 START ACQUISITION]":
            acquisition_found = True

        current_line += 1

    header = lines[current_line]

    outfile.write(f'{header}\n')

def is_mode(line):
    return line.startswith('[')

def is_data_mode(line):
    return line not in NON_DATA_MODES

def extract_mode(mode_line):
    if not mode_line.startswith('['):
        raise ValueError("Invalid mode line")

    match = re.match(r'^\[[^ ]+ (.*)\]', mode_line)
    if match is None:
        raise ValueError("Invalid mode line")

    return match.group(1)

def is_sensor(line):
    return re.match(r'^\w+ [0-9]+$', line) is not None

def extract_sensor(line):
    return re.match(r'^(\w+) [0-9]+$', line).group(1)

def get_sensor_headers(lines):

    # Run through the file. After every mode change (lines containing [PCO2 ???],
    # find the sensor names for that mode as the first word on the line where the
    # first character is not empty.

    # Output
    headers = dict()

    # Modes for state machine
    FIND_DATA_MODE = 0
    PROCESS_DATA_MODE = 1
    FINISHED = 2

    current_line = 0
    current_mode = None
    state = FIND_DATA_MODE

    while state != FINISHED:
        line = lines[current_line]

        
        if state == PROCESS_DATA_MODE:
            if is_sensor(line):
                sensor = extract_sensor(line)
                if sensor not in headers[current_mode]:
                    headers[current_mode].append(extract_sensor(line))

        if is_mode(line):
            if is_data_mode(line):
                current_mode = extract_mode(line)
                if not current_mode in headers:
                    headers[current_mode] = list()

                state = PROCESS_DATA_MODE
            else:
                state = FIND_DATA_MODE

        current_line += 1
        if current_line >= len(lines):
            state = FINISHED

    return headers

def make_data_columns(sensor_headers):
    columns = list()
    for mode, sensors in sensor_headers.items():
        if len(sensors) == 0:
            # This mode has no sensors - just a value.
            columns.append(mode)
        else:
            for sensor in sensors:
                # We need columns for the mean and standard deviation
                columns.append(f'{mode}_{sensor}_mean')
                columns.append(f'{mode}_{sensor}_sd')

    return columns


def parse_position_field(value, hemisphere, negative_hemisphere):
    extract = re.match(r'(\d+)(\d\d.\d+)', value)
    degrees = int(extract.group(1))
    minutes = float(extract.group(2))

    value = degrees + (minutes / 60.0)

    if hemisphere == negative_hemisphere:
        value = value * -1

    return value


def parse_position_line(line):
    fields = line.split(' ')
    lat = parse_position_field(fields[0], fields[1], 'S')
    lon = parse_position_field(fields[2], fields[3], 'W')
    return(lon, lat)

#############################################################
#
# SCRIPT START
#
logging.basicConfig(filename="raw_to_csv.log",
                    format="%(asctime)s:%(levelname)s:%(message)s")
logger = logging.getLogger('raw_to_csv')
logger.setLevel(LOGGING_LEVEL)

parser = argparse.ArgumentParser(
                    prog="raw_to_csv",
                    description="Convert data direct from GENx sensor to CSV for QuinCe")

parser.add_argument("in_file")
parser.add_argument("out_file")

args = parser.parse_args()

logger.log(20, f"Processing file {args.in_file}")

# Read the input file into memory.
# We have to jump around a lot, so this is easier.
with open(args.in_file, 'r') as infile:
    lines = infile.read().splitlines()

# Get the sensor/data names from the file
sensor_headers = get_sensor_headers(lines)

# Make the data columns and add the fixed record columns.
columns = make_data_columns(sensor_headers)
columns = ['Time', 'StatusCode', 'Longitude', 'Latitude'] + columns

df = pd.DataFrame(columns=columns, dtype=float)
df = df.astype({'Time': 'datetime64[ns]', 'StatusCode': str})

# Now for the main data extraction.

# Run through all the lines, doing things based on the
# line content and our current state
SEARCH_FOR_SEQUENCE = 0
MEASUREMENT_SEQUENCE = 1

state = SEARCH_FOR_SEQUENCE

# Current line in file
current_line = 0

# Current count of 'START ACQUISITION' lines seen
current_sequence = 0

# The current mode (from [xxx] lines)
current_mode = None

# The current sensor
current_sensor = None

# Data from the current acquisition
current_acquisition = None

# Store the time of the last acquisition
# so we can detect time anomalies.
last_timestamp = None

# Hold numerical values from the data
values = list()

# Flag indicating whether data is valid.
data_valid = True

while current_line < len(lines):
    line = lines[current_line]

    if state == SEARCH_FOR_SEQUENCE:
        # We aren't currently processing a measurement sequence,
        # so we wait for the start of one.
        if is_mode(line) and extract_mode(line) == 'START ACQUISITION':
            state = MEASUREMENT_SEQUENCE
            current_sequence += 1
            current_acquisition = Acquisition()

    elif state == MEASUREMENT_SEQUENCE:
        if is_mode(line):
            # If we hit a new START ACQUISITION, something went wrong.
            # Discard the current data and start a new set.
            if extract_mode(line) == 'START ACQUISITION':
                logger.log(30, f"Line {current_line}: Incomplete acquisition")
                current_acquisition = Acquisition()
                current_sequence += 1
            else:
                # Add the last set of values to the data
                current_acquisition.add_values(current_mode, current_sensor, values)
                values = list()

                if extract_mode(line) == 'END ACQUISITION':
                    # We have finished the current measurement sequence.
                    # Go back to looking for the next one.
                    state = SEARCH_FOR_SEQUENCE
                    current_acquisition.write(df, current_sequence)
                    current_mode = None
                    current_sensor = None
                    values = list()
                else:
                    current_mode = extract_mode(line)
                    current_sensor = None

                    # If the mode is STATUS, process that specially here
                    if current_mode == 'STATUS':

                        # For STATUS mode, process that now in one shot.
                        
                        # Status code
                        current_line += 1
                        current_acquisition.status = lines[current_line]
                        
                        # Timestamp
                        current_line += 1
                        timestamp = datetime.strptime(lines[current_line], '%Y/%m/%d %H:%M:%S')
                        if (last_timestamp is not None and timestamp < last_timestamp):
                            logger.log(40, f"{current_line}: Timestamps out of sequence")
                            data_valid = False
                            break # Abort

                        current_acquisition.timestamp = timestamp
                        last_timestamp = timestamp

                        # Position
                        current_line += 1
                        (lon, lat) = parse_position_line(lines[current_line])
                        current_acquisition.set_position(lon, lat)

        elif current_mode is not None:
            if is_sensor(line):
                current_acquisition.add_values(current_mode, current_sensor, values)

                # Clear the values array ready for the next sensor's data
                values = list()

                current_sensor = extract_sensor(line)
            else:
                # Split the line into numeric values and add them to the array
                line_values = re.findall(r'(?<!\d)-?\d+(?:\.\d+)?', line)
                values += line_values

    current_line += 1

# Write the data out as CSV
if data_valid:
    df.to_csv(args.out_file, index=False, date_format='%Y-%m-%dT%H:%M:%SZ')
else:
    logging.log(40, "Invalid data - no output written")
    exit(1)
