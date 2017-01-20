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
import logging
import json
import sys
from dlab.fab import *
from dlab.aws_meta import *
from dlab.aws_actions import *
import os
import uuid


# Main function for provisioning notebook server
def run():
    # enable debug level for boto3
    logging.getLogger('botocore').setLevel(logging.DEBUG)
    logging.getLogger('boto3').setLevel(logging.DEBUG)

    instance_class = 'notebook'
    local_log_filename = "{}_{}_{}.log".format(os.environ['resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    try:
        local("~/scripts/{}.py".format('prepare_notebook'))
    except:
        with open("/root/result.json", 'w') as result:
            res = {"error": "Failed preparing Notebook node"}
            print json.dumps(res)
            result.write(json.dumps(res))
        sys.exit(1)

    try:
        local("~/scripts/{}.py".format('configure_zeppelin'))
    except:
        with open("/root/result.json", 'w') as result:
            res = {"error": "Failed configuring Notebook node"}
            print json.dumps(res)
            result.write(json.dumps(res))
        sys.exit(1)


# Main function for terminating exploratory environment
def terminate():
    local_log_filename = "{}_{}_{}.log".format(os.environ['resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)
    try:
        local("~/scripts/{}.py".format('terminate_notebook'))
    except:
        with open("/root/result.json", 'w') as result:
            res = {"error": "Failed terminating Notebook node"}
            print json.dumps(res)
            result.write(json.dumps(res))
        sys.exit(1)


# Main function for stopping notebook server
def stop():
    local_log_filename = "{}_{}_{}.log".format(os.environ['resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] +  "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)
    try:
        local("~/scripts/{}.py".format('stop_notebook'))
    except:
        with open("/root/result.json", 'w') as result:
            res = {"error": "Failed stopping Notebook node"}
            print json.dumps(res)
            result.write(json.dumps(res))
        sys.exit(1)


# Main function for starting notebook server
def start():
    local_log_filename = "{}_{}_{}.log".format(os.environ['resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] +  "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    try:
        local("~/scripts/{}.py".format('start_notebook'))
    except:
        with open("/root/result.json", 'w') as result:
            res = {"error": "Failed starting Notebook node"}
            print json.dumps(res)
            result.write(json.dumps(res))
        sys.exit(1)


# Main function for configuring notebook server after deploying EMR
def configure():
    local_log_filename = "{}_{}_{}.log".format(os.environ['resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] +  "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    try:
        local("~/scripts/{}.py".format('configure_analytic_tool'))
    except:
        with open("/root/result.json", 'w') as result:
            res = {"error": "Failed configuring analytical tool on Notebook node"}
            print json.dumps(res)
            result.write(json.dumps(res))
        sys.exit(1)