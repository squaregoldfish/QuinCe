import argparse
from glob import glob
import json
from netCDF4 import Dataset
import numpy as np
import os
import pandas as pd
import re
import shutil
import sys

# Result codes for check_argo_dir function
ARGO_DIR_CHECK_DOES_NOT_EXIST = -3
ARGO_DIR_CHECK_NOT_DIR = -2
ARGO_DIR_CHECK_UNEXPECTED_FILE = -1
ARGO_DIR_CHECK_EMPTY = 0
ARGO_DIR_CHECK_PARTIAL = 1
ARGO_DIR_CHECK_COMPLETE = 2

ROOT_FILE_STUBS = ['meta', 'prof', 'Rtraj', 'Sprof', 'tech']

parser = argparse.ArgumentParser(
    prog='quince2argo',
    description='Update an Argo netCDF profile dataset with output from QuinCe processing.'
)

parser.add_argument('var_file', type=str, help='Name of a JSON file with the variable settings')
parser.add_argument('profile_id', type=str, help='Argo Profile ID')
parser.add_argument('original_dir', type=str, help='Folder containing original profile netCDF files')
parser.add_argument('quince_file', type=str, help='The QuinCe output file to be applied')
parser.add_argument('output_dir', type=str, help='Folder for updated profile files')
parser.add_argument('-f', '--force', action='store_true', help='Force overwrite of output_dir directory')
args = parser.parse_args()

# Check to see if a directory contains expected Argo netCDF files
def check_argo_dir(profile_id, dir):
    result = None

    if not os.path.exists(dir):
        result = ARGO_DIR_CHECK_DOES_NOT_EXIST
    elif not os.path.isdir(dir):
        result = ARGO_DIR_CHECK_NOT_DIR
    else:
        files = os.listdir(dir)

        if len(files) == 0:
            result = ARGO_DIR_CHECK_EMPTY
        else:
            # Check the profiles directory - we don't check its contents, just that it exists
            profiles_dir_ok = False
            
            profiles_dir = os.path.join(dir, 'profiles')
            if os.path.exists(profiles_dir):
                if not os.path.isdir(profiles_dir):
                    result = ARGO_DIR_CHECK_UNEXPECTED_FILE
                else:
                    profiles_dir_ok = True

            root_files_ok = False
            expected_files = [f'{profile_id}_{x}.nc' for x in ROOT_FILE_STUBS]
            found_files = [os.path.exists(os.path.join(dir, x)) and os.path.isfile(os.path.join(dir, x)) for x in expected_files]

            unexpected_files = [f for f in files if f not in expected_files]
            
            try:
                unexpected_files.remove('profiles')
            except ValueError:
                pass

            if len(unexpected_files) > 0:
                result = ARGO_DIR_CHECK_UNEXPECTED_FILE
            elif False in found_files or not profiles_dir_ok:
                result = ARGO_DIR_CHECK_PARTIAL
            else:
                result = ARGO_DIR_CHECK_COMPLETE

    return result

# Check the input dir
def check_input_dir(args):
    check_result = check_argo_dir(args.profile_id, args.original_dir)

    if check_result in [ARGO_DIR_CHECK_DOES_NOT_EXIST, ARGO_DIR_CHECK_NOT_DIR, ARGO_DIR_CHECK_EMPTY]:
        sys.exit(f'original_dir {args.original_dir} does not exist, is not a directory or is empty')
    elif check_result == ARGO_DIR_CHECK_UNEXPECTED_FILE:
        sys.exit(f'original_dir {args.original_dir} does not look like an Argo profile directory for profile {args.profile_id}')
    elif check_result == ARGO_DIR_CHECK_PARTIAL:
        sys.exit(f'original_dir {args.original_dir} seems to be incomplete')

# Check that the QuinCe file looks as we expect it to
def check_quince_file(quince_file):
    if not os.path.exists(quince_file):
        sys.exit(f'QuinCe file {quince_file} does not exist')
    elif os.path.isdir(quince_file):
        sys.exit(f'QuinCe file {quince_file} is a directory')
    else:
        try:
            df = pd.read_csv(quince_file)
            start_columns = ['SOURCE_FILE', 'SOURCE_FILE_qc', 'CYCLE_NUMBER', 'CYCLE_NUMBER_qc',
                    'DIRECTION', 'DIRECTION_qc', 'NPROF', 'NPROF_qc', 'NLEVEL', 'NLEVEL_qc',
                    'PRES', 'PRES_qc']
            
            if list(df.columns)[:len(start_columns)] != start_columns:
                sys.exit(f'QuinCe file {quince_file} does not look like a QuinCe export file')
        except:
            sys.exit(f'Could not open QuinCe file {quince_file}')

# Check output dir
def check_output_dir(args):
    check_result = check_argo_dir(args.profile_id, args.output_dir)

    if check_result == ARGO_DIR_CHECK_DOES_NOT_EXIST:
        os.mkdir(args.output_dir)
    elif not args.force:
        sys.exit('output_dir {args.output_dir} already exists.')
    else:
        if os.path.isdir(args.output_dir):
            shutil.rmtree(args.output_dir)
        else:
            os.remove(args.output_dir)

        os.mkdir(args.output_dir)

# Check variable settings
def check_var_settings(settings, quince_file):
    quince_columns = pd.read_csv(quince_file).columns
    
    for var in settings:
        if var['quince_var'] not in quince_columns:
            sys.exit(f'QuinCe file does not contain {var["quince_var"]}')

        if var['profile_file_type'] not in ['D', 'BD']:
            sys.exit(f'profile_file_type for {var["quince_var"]} must be either B or BD')


# Get the maximum cycle number from an argo directory
def get_max_cycle_number(dir):
    profiles_dir = os.path.join(dir, 'profiles')
    last_file = sorted(glob("D*[0-9].nc", root_dir=profiles_dir))[-1]
    groups = re.compile(r'.*_([0-9]*)\.nc$').match(last_file).groups()
    return int(groups[0])

def copy_cycle_files(args, cycle_number):
    src_files = glob(f'*_{row_cycle_number:03d}*.nc', root_dir=os.path.join(args.original_dir, 'profiles'))
    for f in src_files:
        shutil.copy(os.path.join(args.original_dir, 'profiles', f), os.path.join(args.output_dir, 'profiles', f))

def load_datasets(profile, dir, cycle_number, direction):

    print(f'Loading {cycle_number} {direction}')

    if direction == 'D':
        file_spec = f'*_{cycle_number:03d}D.nc'
    else:
        file_spec = f'*_{cycle_number:03d}.nc'

    dataset_files = glob(file_spec, root_dir=os.path.join(dir, 'profiles'))

    datasets = {}
    for f in dataset_files:
        dataset_type = re.compile(rf'(.*){profile}_.*').match(f).groups()[0]
        datasets[dataset_type] = Dataset(os.path.join(dir, 'profiles', f), 'r+', format='NETCDF4')

    return datasets

def save_datasets(datasets):
    if len(datasets) > 0:
        for dataset_type, dataset in datasets.items():
            dataset.close()

def copy_var(nc, src, dest):
    src_var = nc.variables[src]

    # Fill value
    kwargs = {}
    if "_FillValue" in src_var.ncattrs():
        kwargs["fill_value"] = src_var.getncattr("_FillValue")

    # Create variable
    dest_var = nc.createVariable(
        dest,
        src_var.datatype,
        src_var.dimensions,
        **kwargs
    )

    # Copy attributes
    dest_var.setncatts({
        name: src_var.getncattr(name) for name in src_var.ncattrs() if name != "_FillValue"
    })

    return dest_var


# Get the output netCDF variable for the specified variable details.
# Create it if necessary.
def get_netcdf_var(var, nc):
    if var['update_argo_var'] in nc.variables:
        nc_var = nc.variables[var['update_argo_var']]
        nc_qc_var = nc.variables[f'{var["update_argo_var"]}_QC']
    else:
        nc_var = copy_var(nc, var['original_argo_var'], var['update_argo_var'])
        nc_qc_var = copy_var(nc, f'{var["original_argo_var"]}_QC', f'{var["update_argo_var"]}_QC')

    return (nc_var, nc_qc_var)


###############################################################################
# Perform pre-flight checks
if args.original_dir == args.output_dir:
    sys.exit('original_dir and output_dir cannot be the same')

check_input_dir(args)
check_quince_file(args.quince_file)
check_output_dir(args)

if not os.path.isfile(args.var_file):
    sys.exit(f'Cannot find/open settings file {args.var_file}')

with open(args.var_file) as sf:
    var_settings = json.load(sf)

check_var_settings(var_settings, args.quince_file)

# Now we can do stuff


# Copy the main files across
for i in ROOT_FILE_STUBS:
    shutil.copy(os.path.join(args.original_dir, f'{args.profile_id}_{i}.nc'), args.output_dir)

# Create the output profiles directory
os.mkdir(os.path.join(args.output_dir, 'profiles'))

## Start processing files

# Track cycle numbers/directions
max_cycle_number = get_max_cycle_number(args.original_dir)
current_cycle_number = None
current_direction = None

# The netCDF file currently being updated
current_datasets = {}

# Load the QuinCe file and process it line by line
quince_data = pd.read_csv(args.quince_file)

for (index, quince_row) in quince_data.iterrows():
    row_cycle_number = int(quince_row['CYCLE_NUMBER'])

    change_datasets = False

    # If we're on a new cycle number, copy the files across
    if row_cycle_number != current_cycle_number:
        change_datasets = True
        current_cycle_number = row_cycle_number
        current_row_direction = None
        copy_cycle_files(args, row_cycle_number)
        
    row_direction = quince_row['DIRECTION']
    if row_direction != current_row_direction:
        change_datasets = True
        current_row_direction = row_direction

    if change_datasets:
        save_datasets(current_datasets)
        current_datasets = load_datasets(args.profile_id, args.output_dir, current_cycle_number, row_direction)
    
    for var in var_settings:

        nc = current_datasets[var['profile_file_type']]
        nc_var, nc_qc_var = get_netcdf_var(var, nc)

        # See if the QuinCe row has this variable in it
        quince_value = quince_row[var['quince_var']]
        qc_flag = quince_row[f'{var["quince_var"]}_qc']
        if not np.isnan(quince_value):
            nprof = quince_row['NPROF'] - 1
            nlevel = quince_row['NLEVEL'] - 1
            nc_var[nprof, nlevel] = quince_value
            nc_qc_var[nprof, nlevel] = qc_flag

save_datasets(current_datasets)