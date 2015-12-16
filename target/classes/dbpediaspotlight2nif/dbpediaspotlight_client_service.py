#!/usr/bin/python3

'''
  File name: dbpedia-spotlight_client_service.py
  Author: Andreas Both
  Date created: 2015-11-19
  Python Version: 3.4
  minimalistic service for retrieving information from DBpedia Spotlight and 
    return them as NIF (which is a RDF vocabulary)
  optionally parameter --example <filename> defines a static example output 
    that is returned for any query
  optionally parameter --port <number> defines the port where the server should
    run, by default it is 8080
  example: 
    ./dbpediaspotlight_client_service.py --example "example1.ttl" --port 8099
'''

import dbpediaspotlight_client
from bottle import route, run, template
import argparse
import sys

def is_example_defined():
  '''
    returns true if example was defined via command line
    @TODO: should be adapted w.r.t. performance if not used for prototyping only
  '''
  if 'example' in parameters and parameters.example:
    print('via command line the example "%s" was set to be the output to ANY query of the service' % (parameters.example))    
    return(True)

def example_ttl_output():
  '''
    returns content of the example file
    if any error appears a valid RDF output is generated containing an error-describing comment
  '''
  if 'example' in parameters and parameters.example:
    examplefile = parameters.example
  else:
    return('# no example file defined')
  try:
    with open(examplefile, 'r') as content_file:
      content = content_file.read()
    return('# example of file %s was used \n%s' % (examplefile, content))
  except Exception as e:
    return('# while reading the example file %s and error appeared: %s ' %(examplefile, e))


# define access to the web service
@route('/<questionstring>')
def index(questionstring):
  if is_example_defined():
    return(example_ttl_output())
  else:
    return(dbpediaspotlight_client.dbpediaspotlight2nif(questionstring))


if __name__ == "__main__":
  # parse the command line parameter
  parser = argparse.ArgumentParser(description='activate an examples with the parameter --example <filename>')
  parser.add_argument('--example',type=str)
  parser.add_argument('--port',type=int)
  parameters = parser.parse_args(sys.argv[1:])
  
  # print 
  if is_example_defined():
    print('the following will be returned to any query:\n%s\n----------\n' % (example_ttl_output(),))
    
  # run server 
  try:
    myport = int(parameters.port)
  except:
    myport = 8080
  run(host='localhost', port=myport)
