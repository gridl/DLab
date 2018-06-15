#!/usr/bin/python

# *****************************************************************************
#
# Copyright (c) 2016, EPAM SYSTEMS INC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ******************************************************************************

import argparse
from fabric.api import run
from fabric.api import sudo
from fabric.api import env
import logging
import traceback
import os
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--cluster_name', type=str, default='')
parser.add_argument('--notebook_ip', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--os_user', type=str, default='')
parser.add_argument('--kernel_action', type=str, default='')
args = parser.parse_args()


def list_directory(cluster_name, kernels_path):
    try:
        kernels = list()
        list_dir = run('python -c "import os, sys; '
                       'print(os.listdir(\'{}\'))"'.format(kernels_path))
        kernels_list = list_dir.replace('[', '').\
            replace(']', '').replace('\'', '').\
            replace(',', '').split()
        for dir_ in kernels_list:
            if 'r_{}'.format(cluster_name) and kernels_config['r_enabled'] == 'true' in dir_:
                kernels.append(dir_)
            elif 'py3spark_{}'.format(cluster_name) in dir_:
                kernels.append(dir_)
            elif 'pyspark_{}'.format(cluster_name) in dir_:
                kernels.append(dir_)
            elif 'toree_{}'.format(cluster_name) in dir_:
                kernels.append(dir_)
            else:
                pass
        return kernels
    except Exception as err:
        logging.error("Failed to build EMR cluster: " +
                     str(err) + "\n Traceback: " +
                     traceback.print_exc(file=sys.stdout))


def enable_data_engine_kernels(cluster_name, default_path, disabled_path):
    try:
        list_kernels = list_directory(cluster_name, disabled_path)
        print
        for kernel in list_kernels:
            sudo('mv {}/{} {}/'.format(disabled_path, kernel, default_path))
    except Exception as err:
        logging.error("Failed to build EMR cluster: " +
                     str(err) + "\n Traceback: " +
                     traceback.print_exc(file=sys.stdout))


def disable_data_engine_kernels(cluster_name, default_path, disabled_path):
    try:
        list_kernels = list_directory(cluster_name, default_path)
        for kernel in list_kernels:
            sudo('mv {}/{} {}/'.format(default_path, kernel, disabled_path))
    except Exception as err:
        logging.error("Failed to build EMR cluster: " +
                     str(err) + "\n Traceback: " +
                     traceback.print_exc(file=sys.stdout))


if __name__ == "__main__":
    env.hosts = "{}".format(args.notebook_ip)
    env.user = args.os_user
    env.key_filename = "{}".format(args.keyfile)
    env.host_string = env.user + "@" + env.hosts
    
    kernels_config = dict()
    kernels_config['default_kernels_path'] = \
        '/home/{}/.local/share/jupyter/kernels'.format(args.os_user)
    kernels_config['disabled_kernels_path'] = \
        '/home/{}/.disabled_kernels'.format(args.os_user)
    kernels_config['r_enabled'] = os.environ['notebook_r_enabled']
    
    if args.kernel_action == 'enable':
        enable_data_engine_kernels(args.cluster_name,
                                   kernels_config['default_kernels_path'],
                                   kernels_config['disabled_kernels_path'])
    elif args.kernel_action == 'disable':
        disable_data_engine_kernels(args.cluster_name,
                                    kernels_config['default_kernels_path'],
                                    kernels_config['disabled_kernels_path'])
    else:
        print('No available actions for Data Engine kernels')
