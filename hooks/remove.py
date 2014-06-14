#!/usr/bin/env python
#
# Copyright 2014 Liferay, Inc. All rights reserved.
# http://www.liferay.com
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
# This is the module project remove hook that will be
# called when your module is remove from a project
#
import os, sys

def dequote(s):
  if s[0:1] == '"':
      return s[1:-1]
        return s

def main(args,argc):
  # You will get the following command line arguments
  # in the following order:
  #
  # project_dir = the full path to the project root directory
  # project_type = the type of project (desktop, mobile, ipad)
  # project_name = the name of the project
  #
  project_dir = dequote(os.path.expanduser(args[1]))
  project_type = dequote(args[2])
  project_name = dequote(args[3])

  # TODO: write your remove hook here (optional)

  # exit
  sys.exit(0)



if __name__ == '__main__':
  main(sys.argv,len(sys.argv))

