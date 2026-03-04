"""
Parse Hagan GenX sensor files into a CSV file that QuinCe can read.
This is similar to the output of the GenX processing software,
except that each stage of an acquisition (measurement sequence)
goes on its own line because it's easier for QuinCe to process.
"""

import argparse
from datetime import datetime, timedelta, timezone
import logging
import numpy as np
import os
import pandas as pd
import re

from warnings import simplefilter
simplefilter(action="ignore", category=pd.errors.PerformanceWarning)

# DEBUG=10, INFO=20, WARNING=30, ERROR=40, CRITICAL=50
LOGGING_LEVEL = 20

# Sensor modes that do not contain measurement data
NON_DATA_MODES = ["[PCO2 START ACQUISITION]", "[PCO2 STATUS]", "[PCO2 END ACQUISITION]"]

# All values are reported as integers, so they need
# multiplying to get them to the right order of magnitude.
SENSOR_MULTIPLIERS = {
    'CO2Calc': 0.01,
    'CO2Temp': 0.01,
    'CO2Pres': 0.001,
    'CO2Raw1': 1,
    'CO2Raw2': 1,
    'RhCalc': 0.01,
    'RhTemp': 0.01,
    'EnclPres': 0.001,
    'EnclAirTemp': 0.01,
    'EnclRH': 0.01,
    'EnclPCBTemp': 0.01
}

# Holds details of an acquisition as it is extracted from the file.
class Acquisition:
    # The span slope value is stored as hex with a fixed offset.
    SPAN_SLOPE_HEX_OFFSET = int('800000', 16)

    def __init__(self, generation):
        self.generation = generation

        self.start_timestamp = None
        self.end_timestamp = None
        self.longitude = None
        self.latitude = None
        self.status = None

        # Signature info
        self.serial_number = None
        self.span_concentration = None
        self.span_slope = None
        self.zero_pre_offset = None
        self.zero_post_offset = None
        self.span_pre_offset = None
        self.span_post_offset = None
        self.air_offset = None
        self.eq_cal_offset = None
        self.eq_between_cal_offset = None
        self.eq_before_sample = None

        self.data = dict()

    def is_complete(self):
        return self.end_timestamp is not None and self.status is not None

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
            if sensor is not None:
                calc_values = calc_values * SENSOR_MULTIPLIERS[sensor]

            mean = np.mean(calc_values)
            stdev = -1 if sensor is None else np.std(calc_values, ddof=1)

            self.data[column_name] = [mean, stdev]

    @staticmethod
    def _get_time_offset(line, line_pos):
        return int(line[line_pos:line_pos + 3], 16)
        

    def set_signature(self, line):
        
        # Extract details from the encoded signature line
        # (after the [PCO2 START ACQUISITION] line)

        # Pos  Len    Type    Desc                                          Sample    Value       Units   Note
        # 0      8    ASC     Instrument Serial Number                      M2406007  M2406007        
        # 8      4    HEX     Span Gas Concentration * 100                  B2AC      457.4       ppm     this will change to len of 5 in GEN3 and may increase to 5 in GEN2 if ref value exceeds 655.35ppm
        # 12     6    HEX     Span Slope Cal K * 10000000 + 0x800000        746239    -0.0761287        
        # 18     3    HEX     Time offset to ZERO pre Licor CAL             110       272         s       seconds from start time to centre of sampling
        # 21     3    HEX     Time offset to ZERO post Licor CAL            136       310         s       seconds from start time to centre of sampling; NOT USED CURRENTLY
        # 24     3    HEX     Time offset to SPAN pre Licor CAL             22B       555         s       seconds from start time to centre of sampling
        # 27     3    HEX     Time offset to SPAN post Licor CAL            251       593         s       seconds from start time to centre of sampling; NOT USED CURRENTLY
        # 30     3    HEX     Time offset to AIR sample                     341       833         s       seconds from start time to centre of sampling
        # 33     3    HEX     Time offset to EQ sample asociated with CAL   A23       2595        s       seconds from start time to centre of sampling
        # 36     3    HEX     Time offset to EQ sample between CAL          31B       795         s       seconds from start time to centre of sampling
        # 39     1    HEX     Equalise EQ Before Sampling                   1         1           -       currently always 1 but future development may make use of this; ONLY GEN3

        line_pos = 0

        # Serial number
        self.serial_number = line[line_pos:8]
        line_pos += 8

        # Span gas concentration
        # Gen2: 4 or 5 characters. Gen3: Always 5
        if self.generation == '2':
            span_conc_length = 4 if len(line) == 39 else 5
        else:
            span_conc_length = 4 if len(line) == 40 else 5

        span_conc_hex = line[line_pos:line_pos + span_conc_length]
        self.span_concentration = int(span_conc_hex, 16) / 100

        line_pos += span_conc_length
        span_slope_hex = line[line_pos:line_pos + 6]
        self.span_slope = (int(span_slope_hex, 16) - self.SPAN_SLOPE_HEX_OFFSET) / 10000000
        
        line_pos += 6
        self.zero_pre_offset = self._get_time_offset(line, line_pos)
        line_pos += 3
        self.zero_post_offset = self._get_time_offset(line, line_pos)
        line_pos += 3
        self.span_pre_offset = self._get_time_offset(line, line_pos)
        line_pos += 3
        self.span_post_offset = self._get_time_offset(line, line_pos)
        line_pos += 3
        self.air_offset = self._get_time_offset(line, line_pos)
        line_pos += 3
        self.eq_cal_offset = self._get_time_offset(line, line_pos)
        line_pos += 3
        self.eq_between_cal_offset = self._get_time_offset(line, line_pos)
        line_pos += 3
        
        if self.generation == '3':
            self.eq_before_sample = int(line[-1:])

    def _calc_offset_timestamp(self, offset_from_start):
        # The various offsets from the signature are from the
        # start of the acquisition to the centre of the thing being measured.
        # We convert these to true timestamps, stored as milliseconds since the epoch.
        return (self.start_timestamp + timedelta(seconds=offset_from_start)).replace(tzinfo=None)


    def write_row(self, df, run_type, timestamp):
        row = df.shape[0] + 1
        df.at[row, 'Time'] = timestamp
        df.at[row, 'RunType'] = run_type
        df.at[row, 'StatusCode'] = self.status
        df.at[row, 'Serial Number'] = self.serial_number
        df.at[row, 'Span Concentration'] = self.span_concentration
        df.at[row, 'Span Slope'] = self.span_slope

        if self.longitude is not None:
            df.at[row, 'Longitude'] = self.longitude
            df.at[row, 'Latitude'] = self.latitude

        df.at[row, 'PUMPON_CO2Calc_AV'] = self.data[f'{run_type}PUMPON_CO2Calc'][0]
        df.at[row, 'PUMPON_CO2Calc_SD'] = self.data[f'{run_type}PUMPON_CO2Calc'][1]
        df.at[row, 'PUMPON_CO2Temp_AV'] = self.data[f'{run_type}PUMPON_CO2Temp'][0]
        df.at[row, 'PUMPON_CO2Temp_SD'] = self.data[f'{run_type}PUMPON_CO2Temp'][1]
        df.at[row, 'PUMPON_CO2Pres_AV'] = self.data[f'{run_type}PUMPON_CO2Pres'][0]
        df.at[row, 'PUMPON_CO2Pres_SD'] = self.data[f'{run_type}PUMPON_CO2Pres'][1]
        df.at[row, 'PUMPON_CO2Raw1_AV'] = self.data[f'{run_type}PUMPON_CO2Raw1'][0]
        df.at[row, 'PUMPON_CO2Raw1_SD'] = self.data[f'{run_type}PUMPON_CO2Raw1'][1]
        df.at[row, 'PUMPON_CO2Raw2_AV'] = self.data[f'{run_type}PUMPON_CO2Raw2'][0]
        df.at[row, 'PUMPON_CO2Raw2_SD'] = self.data[f'{run_type}PUMPON_CO2Raw2'][1]
        df.at[row, 'PUMPON_RhCalc_AV'] = self.data[f'{run_type}PUMPON_RhCalc'][0]
        df.at[row, 'PUMPON_RhCalc_SD'] = self.data[f'{run_type}PUMPON_RhCalc'][1]
        df.at[row, 'PUMPON_RhTemp_AV'] = self.data[f'{run_type}PUMPON_RhTemp'][0]
        df.at[row, 'PUMPON_RhTemp_SD'] = self.data[f'{run_type}PUMPON_RhTemp'][1]

        df.at[row, 'CO2Calc_AV'] = self.data[f'{run_type}_CO2Calc'][0]
        df.at[row, 'CO2Calc_SD'] = self.data[f'{run_type}_CO2Calc'][1]
        df.at[row, 'CO2Temp_AV'] = self.data[f'{run_type}_CO2Temp'][0]
        df.at[row, 'CO2Temp_SD'] = self.data[f'{run_type}_CO2Temp'][1]
        df.at[row, 'CO2Pres_AV'] = self.data[f'{run_type}_CO2Pres'][0]
        df.at[row, 'CO2Pres_SD'] = self.data[f'{run_type}_CO2Pres'][1]
        df.at[row, 'CO2Raw1_AV'] = self.data[f'{run_type}_CO2Raw1'][0]
        df.at[row, 'CO2Raw1_SD'] = self.data[f'{run_type}_CO2Raw1'][1]
        df.at[row, 'CO2Raw2_AV'] = self.data[f'{run_type}_CO2Raw2'][0]
        df.at[row, 'CO2Raw2_SD'] = self.data[f'{run_type}_CO2Raw2'][1]
        df.at[row, 'RhCalc_AV'] = self.data[f'{run_type}_RhCalc'][0]
        df.at[row, 'RhCalc_SD'] = self.data[f'{run_type}_RhCalc'][1]
        df.at[row, 'RhTemp_AV'] = self.data[f'{run_type}_RhTemp'][0]
        df.at[row, 'RhTemp_SD'] = self.data[f'{run_type}_RhTemp'][1]

        if f'{run_type}POST_CO2Calc' in self.data.keys():
            df.at[row, 'POST_CO2Calc_AV'] = self.data[f'{run_type}POST_CO2Calc'][0]
            df.at[row, 'POST_CO2Calc_SD'] = self.data[f'{run_type}POST_CO2Calc'][1]
            df.at[row, 'POST_CO2Temp_AV'] = self.data[f'{run_type}POST_CO2Temp'][0]
            df.at[row, 'POST_CO2Temp_SD'] = self.data[f'{run_type}POST_CO2Temp'][1]
            df.at[row, 'POST_CO2Pres_AV'] = self.data[f'{run_type}POST_CO2Pres'][0]
            df.at[row, 'POST_CO2Pres_SD'] = self.data[f'{run_type}POST_CO2Pres'][1]
            df.at[row, 'POST_CO2Raw1_AV'] = self.data[f'{run_type}POST_CO2Raw1'][0]
            df.at[row, 'POST_CO2Raw1_SD'] = self.data[f'{run_type}POST_CO2Raw1'][1]
            df.at[row, 'POST_CO2Raw2_AV'] = self.data[f'{run_type}POST_CO2Raw2'][0]
            df.at[row, 'POST_CO2Raw2_SD'] = self.data[f'{run_type}POST_CO2Raw2'][1]
            df.at[row, 'POST_RhCalc_AV'] = self.data[f'{run_type}POST_RhCalc'][0]
            df.at[row, 'POST_RhCalc_SD'] = self.data[f'{run_type}POST_RhCalc'][1]
            df.at[row, 'POST_RhTemp_AV'] = self.data[f'{run_type}POST_RhTemp'][0]
            df.at[row, 'POST_RhTemp_SD'] = self.data[f'{run_type}POST_RhTemp'][1]

    
    def write(self, df):
        # If this acquisition is not complete, do nothing.
        if self.is_complete():
            if 'ZERO_CO2Calc' in self.data.keys():
                self.write_row(df, 'ZERO', self._calc_offset_timestamp(self.zero_post_offset))

            if 'SPAN_CO2Calc' in self.data.keys():
                self.write_row(df, 'SPAN', self._calc_offset_timestamp(self.span_post_offset))

            if ('AIR_CO2Calc' in self.data.keys()) :
                self.write_row(df, 'AIR', self._calc_offset_timestamp(self.air_offset))

            if ('EQ_CO2Calc' in self.data.keys()) :
                self.write_row(df, 'EQ', self.end_timestamp.replace(tzinfo=None))


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
    return line.startswith('[') and not line.startswith('[AUX')

def is_aux(line):
    return line.startswith('[AUX')

def get_aux_type(aux_line):
    match = re.match(r'\[AUX (.*)\]', aux_line)
    if match is None:
        raise ValueError("Invalid AUX mode line")

    return match.group(1)

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
    
    # Extract the columns from the input file
    file_columns = list()
    for mode, sensors in sensor_headers.items():
        if len(sensors) == 0:
            # This mode has no sensors - just a value.
            file_columns.append(mode)
        else:
            for sensor in sensors:
                # We need columns for the mean and standard deviation
                file_columns.append(f'{mode}_{sensor}_AV')
                file_columns.append(f'{mode}_{sensor}_SD')


    # Reorder the columns
    output_columns = list()

    for col in OUTPUT_COLUMN_ORDER:
        if col in file_columns:
            output_columns.append(col)
            file_columns.remove(col)

    output_columns = output_columns + file_columns

    return output_columns


def parse_position_field(value, hemisphere, negative_hemisphere):
    extract = re.match(r'(\d+)(\d\d.\d+)', value)
    degrees = int(extract.group(1))
    minutes = float(extract.group(2))

    value = degrees + (minutes / 60.0)

    if hemisphere == negative_hemisphere:
        value = value * -1

    return value


def parse_position_line(line, use_ddmm_pos):
    fields = line.split()
    
    if use_ddmm_pos:
        lat = parse_position_field(fields[0], fields[1], 'S')
        lon = parse_position_field(fields[2], fields[3], 'W')
    else:
        lon = fields[0]
        lat = fields[1]

    return(lon, lat)

def get_timestamp(line):
    return datetime.strptime(line, '%Y/%m/%d %H:%M:%S').replace(tzinfo=timezone.utc)

def extract_values(line):
    values = list()

    current_value = ''
    current_pos = 0

    while current_pos < len(line):
        if (line[current_pos] == ' ' or line[current_pos] == '-'):
            if len(current_value) > 0:
                values.append(current_value)
                current_value = ''
        
        if line[current_pos] != ' ':
            current_value += line[current_pos]

        current_pos += 1

    if len(current_value) > 0:
        values.append(current_value)

    return values

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

parser.add_argument("--ddmm_pos", action='store_true', help='Use DDMM.MMM position format')
parser.add_argument("generation", help="Sensor generation", choices=['2', '3'])
parser.add_argument("in_file", help="Input SD card file")
parser.add_argument("out_file_root", help="Root of output file(s). Suffixes will be added automatically.")

args = parser.parse_args()

# If the output root is a folder, we abort. The root must be a path and the beginning of a filename
if os.path.isdir(args.out_file_root):
    print('ERROR: out_file_root must not be a directory. It must be a path with a filename prefix')
    print('e.g. /home/temp/output will generate /home/temp/output_<files>')
    exit(1)

logger.log(20, f"Processing file {args.in_file}")

# Read the input file into memory.
# We have to jump around a lot, so this is easier.
with open(args.in_file, 'r') as infile:
    lines = infile.read().splitlines()

# Get the sensor/data names from the file
sensor_headers = get_sensor_headers(lines)

# Create the destination DataFrame with columns
columns = ['Time', 'RunType', 'StatusCode', 'Serial Number', 'Span Concentration',
    'Span Slope', 'Longitude', 'Latitude', 
    'PUMPON_CO2Calc_AV', 'PUMPON_CO2Calc_SD', 'PUMPON_CO2Temp_AV', 'PUMPON_CO2Temp_SD',
    'PUMPON_CO2Pres_AV', 'PUMPON_CO2Pres_SD', 'PUMPON_CO2Raw1_AV', 'PUMPON_CO2Raw1_SD',
    'PUMPON_CO2Raw2_AV', 'PUMPON_CO2Raw2_SD', 'PUMPON_RhCalc_AV', 'PUMPON_RhCalc_SD',
    'PUMPON_RhTemp_AV', 'PUMPON_RhTemp_SD',
    'CO2Calc_AV', 'CO2Calc_SD', 'CO2Temp_AV', 'CO2Temp_SD', 'CO2Pres_AV', 'CO2Pres_SD',
    'CO2Raw1_AV', 'CO2Raw1_SD', 'CO2Raw2_AV', 'CO2Raw2_SD', 'RhCalc_AV', 'RhCalc_SD', 'RhTemp_AV', 'RhTemp_SD',
    'POST_CO2Calc_AV', 'POST_CO2Calc_SD', 'POST_CO2Temp_AV', 'POST_CO2Temp_SD',
    'POST_CO2Pres_AV', 'POST_CO2Pres_SD', 'POST_CO2Raw1_AV', 'POST_CO2Raw1_SD', 'POST_CO2Raw2_AV',
    'POST_CO2Raw2_SD', 'POST_RhCalc_AV', 'POST_RhCalc_SD', 'RhTemp_AV', 'RhTemp_SD']

df = pd.DataFrame(columns=columns, dtype=float)
df = df.astype({'Time': 'datetime64[ns]', 'RunType': str, 'StatusCode': str, 'Serial Number': str})


# Now for the main data extraction.

# Run through all the lines, doing things based on the
# line content and our current state
SEARCH_FOR_SEQUENCE = 0
MEASUREMENT_SEQUENCE = 1

# We start by looking for the beginning of an ACQUISITION sequence
state = SEARCH_FOR_SEQUENCE

# Current line in file
current_line = 0

# Current count of 'START ACQUISITION' lines seen
current_sequence = 0

# The current mode (from lines containing [???])
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

# Aux file handles.
# Aux lines (defined by lines of the form'[AUX ???]')
# are written to their own files as they are encountered. Each ??? is its own file.
aux_data = dict()

while current_line < len(lines):
    line = lines[current_line]

    if state == SEARCH_FOR_SEQUENCE:
        # We aren't currently processing a measurement sequence (aka Acquisition),
        # so we wait for the start of one.
        if is_mode(line) and extract_mode(line) == 'START ACQUISITION':
            state = MEASUREMENT_SEQUENCE
            current_sequence += 1
            current_acquisition = Acquisition(args.generation)
            
            # The next line contains the acquisition signature
            current_line += 1
            current_acquisition.set_signature(lines[current_line])
            
            # After that is the acquisition's timestamp. Parse it, and if fail,
            # log the error and move on to the next acquisition.
            current_line += 1

            try:
                current_acquisition.start_timestamp = get_timestamp(lines[current_line])
            except ValueError:
                logger.log(30, f"Line {current_line}: Invalid acquisition timestamp")
                state = SEARCH_FOR_SEQUENCE

    elif state == MEASUREMENT_SEQUENCE:
        if is_aux(line):
            
            # Add the next line to the appropriate AUX file
            aux_type = get_aux_type(line)
            
            if aux_type not in aux_data.keys():
                aux_data[aux_type] = ''
            
            current_line += 1
            aux_data[aux_type] = aux_data[aux_type] + lines[current_line] + '\n'

        elif is_mode(line):
            # If we hit a new START ACQUISITION, something went wrong.
            # Discard the current data and start a new set.
            if extract_mode(line) == 'START ACQUISITION':
                logger.log(30, f"Line {current_line}: Incomplete acquisition")
                current_acquisition = Acquisition(args.generation)
                values = list()
                current_mode = None
                current_sequence += 1

                current_line += 1
                current_acquisition.set_signature(lines[current_line])
                current_line += 1

                try:
                    current_acquisition.start_timestamp = get_timestamp(lines[current_line])
                except ValueError:
                    logger.log(30, f"Line {current_line}: Invalid acquisition timestamp")
                    state = SEARCH_FOR_SEQUENCE
            else:
                # Add the last set of values to the data
                current_acquisition.add_values(current_mode, current_sensor, values)
                values = list()

                if extract_mode(line) == 'END ACQUISITION':
                    # We have finished the current measurement sequence.
                    # Go back to looking for the next one.
                    state = SEARCH_FOR_SEQUENCE
                    current_acquisition.write(df)
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
                        timestamp = get_timestamp(lines[current_line])
                        if (last_timestamp is not None and timestamp < last_timestamp):
                            logger.log(40, f"{current_line}: Timestamps out of sequence")
                            data_valid = False
                            break # Abort

                        current_acquisition.end_timestamp = timestamp
                        last_timestamp = timestamp

                        # Position
                        current_line += 1
                        (lon, lat) = parse_position_line(lines[current_line], args.ddmm_pos)
                        current_acquisition.set_position(lon, lat)

        elif current_mode is not None:
            if is_sensor(line):
                current_acquisition.add_values(current_mode, current_sensor, values)

                # Clear the values array ready for the next sensor's data
                values = list()

                current_sensor = extract_sensor(line)
            else:
                # Split the line into numeric values and add them to the array
                values += extract_values(line)

    current_line += 1

# Write the data out as CSV
if data_valid:

    # Coerce columns
    # if args.generation == '2':
    #     df = df.astype({'Time': 'datetime64[ns]', 'StatusCode': str, 'Serial Number': str,
    #         'Zero Pre Time': int, 'Zero Post Time': int, 'Span Pre Time': int,
    #         'Span Post Time': int, 'Air Time': int, 'Eq Between CAL Time': int, 'Eq CAL Time': int})
    # else:
    #     df = df.astype({'Time': 'datetime64[ns]', 'StatusCode': str, 'Serial Number': str,
    #         'Zero Pre Time': int, 'Zero Post Time': int, 'Span Pre Time': int,
    #         'Span Post Time': int, 'Air Time': int, 'Eq Between CAL Time': int, 'Eq CAL Time': int,
    #         'Eq Before Sample': int})

    # Write the main data
    df.to_csv(f'{args.out_file_root}.csv', index=False, date_format='%Y-%m-%dT%H:%M:%SZ', na_rep='NaN')

    # Write each AUX data to its file
    for (aux_type, contents) in aux_data.items():
        with open(f'{args.out_file_root}_{aux_type}.dat', 'w') as aux_out:
            aux_out.write(contents)
else:
    logging.log(40, "Invalid data - no output written")
    exit(1)
