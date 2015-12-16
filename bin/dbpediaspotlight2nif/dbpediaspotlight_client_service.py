#!/usr/bin/python3

import dbpediaspotlight_client
from bottle import route, run, template

@route('/<questionstring>')
def index(questionstring):
  #return template('<b>Hello {{name}}</b>!', name=name)
  return(dbpediaspotlight_client.dbpediaspotlight2nif(questionstring))

if __name__ == "__main__":
  run(host='localhost', port=8080)
