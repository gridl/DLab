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
import json
from dlab.actions_lib import *
from dlab.meta_lib import *
import sys


parser = argparse.ArgumentParser()
parser.add_argument('--instance_name', type=str, default='')
parser.add_argument('--region', type=str, default='')
parser.add_argument('--zone', type=str, default='')
parser.add_argument('--vpc_name', type=str, default='')
parser.add_argument('--subnet_name', type=str, default='')
parser.add_argument('--instance_size', type=str, default='')
parser.add_argument('--ssh_key_path', type=str, default='')
args = parser.parse_args()


if __name__ == "__main__":
    if args.instance_name:
        if GCPMeta().get_instance(args.instance_name):
            print "REQUESTED INSTANCE {} ALREADY EXISTS".format(args.instance_name)
        else:
            print "Creating Instance {}".format(args.instance_name)
            GCPActions().create_instance(args.instance_name, args.region, args.zone, args.vpc_name, args.subnet_name,
                                         args.instance_size, args.ssh_key_path)
    else:
        sys.exit(1)