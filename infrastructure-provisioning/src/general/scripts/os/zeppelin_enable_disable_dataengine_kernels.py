import argparse
import json
import logging
import requests
import sys
from fabric.api import env
from fabric.api import sudo
from fabric.contrib.files import exists
from fabric.api import run
from fabric.api import get
from fabric.api import put
from fabric.api import local


parser = argparse.ArgumentParser()
parser.add_argument('--cluster_name', type=str, default='')
parser.add_argument('--notebook_ip', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--os_user', type=str, default='')
parser.add_argument('--kernel_action', type=str, default='')
args = parser.parse_args()


def kernel_list(kernel_path):
    try:
        list_dir = run('python -c "import os, sys; '
                       'print(os.listdir(\'{}\'))"'.format(kernel_path))
        kernels_list = list_dir.replace('[', ''). \
            replace(']', '').replace('\'', ''). \
            replace(',', '').split()
        kernels_ = ('{}_py2.json', '{}_py3.json')
        ker = (map(lambda kernel_name: kernel_name.format(args.cluster_name),
                   [x for x in kernels_]))
        list_kernels = (set(ker) & set(kernels_list))
        return list(list_kernels)
    except Exception as err:
        print('Failed to get list of interpreters: ', str(err))
        sys.exit(1)


def disable_data_engine_kernels(interpreter_name):
    print('DISABLING THE INTERPRETERS {}'.format(interpreter_name))
    try:
        interpreter_id = list()
        interpreter_ = list()
        local('mkdir /tmp/zeppelin_interpreters')
        get('/opt/zeppelin/conf/interpreter.json',
            '/tmp/zeppelin_interpreters/interpreter.json')
    
        with open('/tmp/zeppelin_interpreters/interpreter.json', 'r') as conf:
            data = json.loads(conf.read())
        for i in data.get('interpreterSettings').keys():
            if data.get('interpreterSettings').get(i).get(
                    'name') in interpreter_name:
                with open('/tmp/zeppelin_interpreters/{}.json'.format(
                        data['interpreterSettings'].
                                get(i).get('name')), 'w') as inter:
                    json.dump(data['interpreterSettings'].get(i), inter)
                interpreter_id.append(i)
                interpreter_.append(data['interpreterSettings'].get(i).get('name'))
        if not exists('/opt/.disabled_kernels'):
            sudo('mkdir /opt/.disabled_kernels')
        for j in interpreter_:
            put('/tmp/zeppelin_interpreters/{}'.format(j), '/opt/.disabled_kernels/',
                use_sudo=True)
        for id_ in interpreter_id:
            req = requests.delete(
                'http://{}:8080/api/interpreter/setting/{}'.format(
                    args.notebook_ip,
                    id_))
            print('The interpreter {} was disabled with status code{}'.format(
                id_,
                req.status_code))
    except Exception as err:
        logging.error("Failed to disable data engine interpreters: " + str(err))
        sys.exit(1)


def enable_data_engine_kernels(kernel_path, ker):
    print('ENABLING INTERPRETERS'.format(ker))
    try:
        k_ = kernel_list(kernel_path)
        for i in k_:
            get(remote_path="/home/{}/.disabled_kernels/{}".format(args.os_user, i),
                local_path="/tmp/")
        for k_name in k_:
            with open('/tmp/{}'.format(k_name), 'r') as d:
                interp = json.loads(d.read())
            dm = json.dumps(interp)
            req = requests.post(
                'http://{}:8080/api/interpreter/setting'.format(args.notebook_ip),
                data=dm)
            print('INTERPRETERS {} WERE ENABLED'.format(k_name, req.status_code))
        for j in k_:
            sudo('rm -rf /opt/.disabled_kernels/{}'.format(j))
    except Exception as err:
        logging.error("Failed to enable data engine interpreters: " + str(err))
        sys.exit(1)


if __name__ == "__main__":
    env.hosts = "{}".format(args.notebook_ip)
    env.user = args.os_user
    env.key_filename = "{}".format(args.keyfile)
    env.host_string = env.user + "@" + env.hosts
    kernels_ = ('{}_py2', '{}_py3')
    ker = (map(lambda kernel_name: kernel_name.format(args.cluster_name),
               [x for x in kernels_]))
    kernel_path = '/opt/.disabled_kernels'
    if args.kernel_action == 'disable':
        disable_data_engine_kernels(ker)
    elif args.kernel_action == 'enable':
        enable_data_engine_kernels(kernel_path, ker)
