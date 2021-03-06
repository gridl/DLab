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

import json
import time
from fabric.api import *
from dlab.fab import *
from dlab.meta_lib import *
from dlab.actions_lib import *
import sys
import os
import uuid
import logging
from Crypto.PublicKey import RSA
import multiprocessing


def configure_slave(slave_number, data_engine):
    slave_name = data_engine['slave_node_name'] + '{}'.format(slave_number + 1)
    slave_hostname = get_instance_private_ip_address(data_engine['tag_name'], slave_name)
    try:
        logging.info('[CREATING DLAB SSH USER ON SLAVE NODE]')
        print('[CREATING DLAB SSH USER ON SLAVE NODE]')
        params = "--hostname {} --keyfile {} --initial_user {} --os_user {} --sudo_group {}".format \
            (slave_hostname, os.environ['conf_key_dir'] + data_engine['key_name'] + ".pem", initial_user,
             data_engine['dlab_ssh_user'], sudo_group)

        try:
            local("~/scripts/{}.py {}".format('create_ssh_user', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        append_result("Failed to create ssh user on slave.", str(err))
        sys.exit(1)

    try:
        logging.info('[CLEANING INSTANCE FOR SLAVE NODE]')
        print('[CLEANING INSTANCE FOR SLAVE NODE]')
        params = '--hostname {} --keyfile {} --os_user {} --application {}' \
            .format(slave_hostname, keyfile_name, data_engine['dlab_ssh_user'], os.environ['application'])
        try:
            local("~/scripts/{}.py {}".format('common_clean_instance', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        append_result("Failed to clean slave instance.", str(err))
        sys.exit(1)

    try:
        logging.info('[CONFIGURE PROXY ON SLAVE NODE]')
        print('[CONFIGURE PROXY ON ON SLAVE NODE]')
        additional_config = {"proxy_host": edge_instance_hostname, "proxy_port": "3128"}
        params = "--hostname {} --instance_name {} --keyfile {} --additional_config '{}' --os_user {}"\
            .format(slave_hostname, slave_name, keyfile_name, json.dumps(additional_config),
                    data_engine['dlab_ssh_user'])
        try:
            local("~/scripts/{}.py {}".format('common_configure_proxy', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        append_result("Failed to configure proxy on slave.", str(err))
        sys.exit(1)

    try:
        logging.info('[INSTALLING PREREQUISITES ON SLAVE NODE]')
        print('[INSTALLING PREREQUISITES ON SLAVE NODE]')
        params = "--hostname {} --keyfile {} --user {} --region {}". \
            format(slave_hostname, keyfile_name, data_engine['dlab_ssh_user'], data_engine['region'])
        try:
            local("~/scripts/{}.py {}".format('install_prerequisites', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        append_result("Failed to install prerequisites on slave.", str(err))
        sys.exit(1)

    try:
        logging.info('[CONFIGURE SLAVE NODE {}]'.format(slave + 1))
        print('[CONFIGURE SLAVE NODE {}]'.format(slave + 1))
        params = "--hostname {} --keyfile {} --region {} --spark_version {} --hadoop_version {} --os_user {} --scala_version {} --r_mirror {} --master_ip {} --node_type {}". \
            format(slave_hostname, keyfile_name, data_engine['region'], os.environ['notebook_spark_version'],
                   os.environ['notebook_hadoop_version'], data_engine['dlab_ssh_user'],
                   os.environ['notebook_scala_version'], os.environ['notebook_r_mirror'], master_node_hostname,
                   'slave')
        try:
            local("~/scripts/{}.py {}".format('configure_dataengine', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        append_result("Failed to configure slave node.", str(err))
        sys.exit(1)


if __name__ == "__main__":
    local_log_filename = "{}_{}_{}.log".format(os.environ['conf_resource'], os.environ['edge_user_name'],
                                               os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['conf_resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.INFO,
                        filename=local_log_filepath)

    try:
        print('Generating infrastructure names and tags')
        data_engine = dict()
        try:
            data_engine['exploratory_name'] = os.environ['exploratory_name']
        except:
            data_engine['exploratory_name'] = ''
        try:
            data_engine['computational_name'] = os.environ['computational_name']
        except:
            data_engine['computational_name'] = ''
        data_engine['service_base_name'] = os.environ['conf_service_base_name']
        data_engine['tag_name'] = data_engine['service_base_name'] + '-Tag'
        data_engine['key_name'] = os.environ['conf_key_name']
        data_engine['region'] = os.environ['aws_region']
        data_engine['cluster_name'] = data_engine['service_base_name'] + '-' + os.environ['edge_user_name'] + \
                                      '-de-' + data_engine['exploratory_name'] + '-' + \
                                      data_engine['computational_name']
        data_engine['master_node_name'] = data_engine['cluster_name'] + '-m'
        data_engine['slave_node_name'] = data_engine['cluster_name'] + '-s'
        data_engine['master_size'] = os.environ['aws_dataengine_master_shape']
        data_engine['slave_size'] = os.environ['aws_dataengine_slave_shape']
        data_engine['dataengine_master_security_group_name'] = data_engine['service_base_name'] + '-' + \
                                                               os.environ['edge_user_name'] + '-dataengine-master-sg'
        data_engine['dataengine_slave_security_group_name'] = data_engine['service_base_name'] + '-' + \
                                                              os.environ['edge_user_name'] + '-dataengine-slave-sg'
        data_engine['tag_name'] = data_engine['service_base_name'] + '-Tag'
        tag = {"Key": data_engine['tag_name'],
               "Value": "{}-{}-subnet".format(data_engine['service_base_name'], os.environ['edge_user_name'])}
        data_engine['subnet_cidr'] = get_subnet_by_tag(tag)
        data_engine['notebook_dataengine_role_profile_name'] = data_engine['service_base_name']. \
                                                                   lower().replace('-', '_') + "-" + \
                                                               os.environ['edge_user_name'] + '-nb-de-Profile'
        data_engine['instance_count'] = int(os.environ['dataengine_instance_count'])
        master_node_hostname = get_instance_hostname(data_engine['tag_name'], data_engine['master_node_name'])
        data_engine['dlab_ssh_user'] = os.environ['conf_os_user']
        keyfile_name = "{}{}.pem".format(os.environ['conf_key_dir'], os.environ['conf_key_name'])
        edge_instance_name = os.environ['conf_service_base_name'] + "-" + os.environ['edge_user_name'] + '-edge'
        edge_instance_hostname = get_instance_hostname(data_engine['tag_name'], edge_instance_name)

        if os.environ['conf_os_family'] == 'debian':
            initial_user = 'ubuntu'
            sudo_group = 'sudo'
        if os.environ['conf_os_family'] == 'redhat':
            initial_user = 'ec2-user'
            sudo_group = 'wheel'
    except Exception as err:
        data_engine['tag_name'] = data_engine['service_base_name'] + '-Tag'
        data_engine['cluster_name'] = data_engine['service_base_name'] + '-' + os.environ['edge_user_name'] + \
                                      '-de-' + data_engine['exploratory_name'] + '-' + \
                                      data_engine['computational_name']
        data_engine['master_node_name'] = data_engine['cluster_name'] + '-m'
        data_engine['slave_node_name'] = data_engine['cluster_name'] + '-s'
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(int(os.environ['dataengine_instance_count']) - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        print("Failed to generate variables dictionary.")
        append_result("Failed to generate variables dictionary.", str(err))
        sys.exit(1)

    try:
        logging.info('[CREATING DLAB SSH USER ON MASTER NODE]')
        print('[CREATING DLAB SSH USER ON MASTER NODE]')
        params = "--hostname {} --keyfile {} --initial_user {} --os_user {} --sudo_group {}".format\
            (master_node_hostname, os.environ['conf_key_dir'] + data_engine['key_name'] + ".pem", initial_user,
             data_engine['dlab_ssh_user'], sudo_group)

        try:
            local("~/scripts/{}.py {}".format('create_ssh_user', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        append_result("Failed to create ssh user on master.", str(err))
        sys.exit(1)

    try:
        logging.info('[CLEANING INSTANCE FOR MASTER NODE]')
        print('[CLEANING INSTANCE FOR MASTER NODE]')
        params = '--hostname {} --keyfile {} --os_user {} --application {}' \
            .format(master_node_hostname, keyfile_name, data_engine['dlab_ssh_user'], os.environ['application'])
        try:
            local("~/scripts/{}.py {}".format('common_clean_instance', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        append_result("Failed to clean master instance.", str(err))
        sys.exit(1)

    try:
        logging.info('[CONFIGURE PROXY ON MASTER NODE]')
        print('[CONFIGURE PROXY ON ON MASTER NODE]')
        additional_config = {"proxy_host": edge_instance_hostname, "proxy_port": "3128"}
        params = "--hostname {} --instance_name {} --keyfile {} --additional_config '{}' --os_user {}"\
            .format(master_node_hostname, data_engine['master_node_name'], keyfile_name, json.dumps(additional_config),
                    data_engine['dlab_ssh_user'])
        try:
            local("~/scripts/{}.py {}".format('common_configure_proxy', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        append_result("Failed to configure proxy on master.", str(err))
        sys.exit(1)

    try:
        logging.info('[INSTALLING PREREQUISITES ON MASTER NODE]')
        print('[INSTALLING PREREQUISITES ON MASTER NODE]')
        params = "--hostname {} --keyfile {} --user {} --region {}".\
            format(master_node_hostname, keyfile_name, data_engine['dlab_ssh_user'], data_engine['region'])
        try:
            local("~/scripts/{}.py {}".format('install_prerequisites', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        append_result("Failed to install prerequisites on master.", str(err))
        sys.exit(1)

    try:
        logging.info('[CONFIGURE MASTER NODE]')
        print('[CONFIGURE MASTER NODE]')
        params = "--hostname {} --keyfile {} --region {} --spark_version {} --hadoop_version {} --os_user {} --scala_version {} --r_mirror {} --master_ip {} --node_type {}".\
            format(master_node_hostname, keyfile_name, data_engine['region'], os.environ['notebook_spark_version'],
                   os.environ['notebook_hadoop_version'], data_engine['dlab_ssh_user'],
                   os.environ['notebook_scala_version'], os.environ['notebook_r_mirror'], master_node_hostname,
                   'master')
        try:
            local("~/scripts/{}.py {}".format('configure_dataengine', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        append_result("Failed to configure master node", str(err))
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        sys.exit(1)

    try:
        jobs = []
        for slave in range(data_engine['instance_count'] - 1):
            p = multiprocessing.Process(target=configure_slave, args=(slave, data_engine))
            jobs.append(p)
            p.start()
        for job in jobs:
            job.join()
        for job in jobs:
            if job.exitcode != 0:
                raise Exception
    except Exception as err:
        remove_ec2(data_engine['tag_name'], data_engine['master_node_name'])
        for i in range(data_engine['instance_count'] - 1):
            slave_name = data_engine['slave_node_name'] + '{}'.format(i + 1)
            remove_ec2(data_engine['tag_name'], slave_name)
        sys.exit(1)


    try:
        logging.info('[SUMMARY]')
        print('[SUMMARY]')
        print("Service base name: {}".format(data_engine['service_base_name']))
        print("Region: {}".format(data_engine['region']))
        print("Cluster name: {}".format(data_engine['cluster_name']))
        print("Master node shape: {}".format(data_engine['master_size']))
        print("Slave node shape: {}".format(data_engine['slave_size']))
        print("Instance count: {}".format(str(data_engine['instance_count'])))
        with open("/root/result.json", 'w') as result:
            res = {"hostname": data_engine['cluster_name'],
                   "instance_id": get_instance_by_name(data_engine['tag_name'], data_engine['master_node_name']),
                   "key_name": data_engine['key_name'],
                   "Action": "Create new Data Engine"}
            print(json.dumps(res))
            result.write(json.dumps(res))
    except:
        print("Failed writing results.")
        sys.exit(0)
