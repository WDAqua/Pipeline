#!/usr/bin/python3

'''
  File name: dbpedia-spotlight_client.py
  Author: Andreas Both
  Date created: 2015-11-19
  Python Version: 3.4
  prototypical client of the DBpedia Spotlight service translating JSON to NIF
  optional 1st parameter, e.g., 'Obama New York', 'Barack Obama in London'
  called by dbpediaspotlight2nif('Obama New York') 
         or dbpediaspotlight2nif('Obama New York', uriOfService) 
'''

import urllib.parse
import urllib.request
import json
import sys
import uuid
from io import StringIO

def dbpediaspotlight2nif(myText,myServiceUrl = 'http://spotlight.dbpedia.org/rest/candidates'):
  '''
    calls DBpedia 
  '''
  
  # configuration
  myParams = urllib.parse.urlencode({
        'text':myText,
        'confidence':'0',
        'support':'0',
        'spotter':'Default',
        'disambiguator':'Default',
        'policy':'whitelist'
      }).encode('utf-8');
  # keys which should be translated into NIF
  allowedKeys = {
        '@finalScore':'nif:confidence',
        '@uri':'itsrdf:taIdentRef'
      }
  basekey='<#char=0,%d>' % (len(myText))
  myString = StringIO()

  # fetch data from web service
  myRequest = urllib.request.Request(myServiceUrl,myParams)
  myRequest.add_header('Accept','application/json')
  myRequest.add_header('User-Agent','NIF2.1 wrapper prototype')
  myResponse = urllib.request.urlopen(myRequest).read()


  # transform response data to nif relations
  data = json.loads(myResponse.decode('utf-8'))


  surfaceforms = data['annotation']['surfaceForm']
  entityData = {} # need to capture the keys to prevent duplicates
  for surfaceform in surfaceforms:
    #print(json.dumps(surfaceform, indent=4, sort_keys=True))
    myData = {
        'nif:anchorOf':surfaceform['@name'],
        'nif:beginIndex':int(surfaceform['@offset']),
        'nif:endIndex':int(surfaceform['@offset'])+len(surfaceform['@name']),
        'nif:referenceContext':basekey
      }

    if 'resource' in surfaceform:
      # sometimes no list is delivered as resource field
      if not isinstance(surfaceform['resource'], list):
        surfaceform['resource'] = [surfaceform['resource']]
      
      for r in surfaceform['resource']:
        resourceData = myData.copy()
        for k in r:
          if k in allowedKeys:
            resourceData[allowedKeys[k]] = r[k]
        
        # extend the resource URI part to an actual resource URI
        if 'itsrdf:taIdentRef' in resourceData:
          resourceData['itsrdf:taIdentRef'] = 'http://dbpedia.org/resource/'+resourceData['itsrdf:taIdentRef']
          key = '%s %s %s' % (resourceData['nif:beginIndex'],resourceData['nif:endIndex'],resourceData['itsrdf:taIdentRef'])
        else:
          key = '%s %s' % (resourceData['nif:beginIndex'],resourceData['nif:endIndex'])

        entityData[key] = resourceData
    else:
      key = '%s %s' % (myData['nif:beginIndex'],myData['nif:endIndex'])
      entityData[key] = myData


  def myformat(v):
    '''
      format values of output
    '''
    if v == '<>':
      return '<>'
    elif isinstance(v,int):
      return '"%s"%s' % (v,'^^xsd:nonNegativeInteger')
    elif isinstance(v,float):
      return '"%s"%s' % (v,'^^xsd:decimal')
    else:
      return '"%s"' % (v)

  # define referenceContext
  entityData[basekey] = {
      'nif:beginIndex':0,
      'nif:endIndex':len(myText),
      'a':'nif:Context',
      'nif:isString':myText,
      'nif:sourceUrl':'<>'  # same as @base
    }


  # print NIF 
  myString.write('# input: %s\n' % myText )
  # print prefix 
  myString.write('@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n')
  myString.write('@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n')
  myString.write('@prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n\n')
  myString.write('@base <urn:uuid:%s>\n\n' % (uuid.uuid4()))

  # print all entities
  for k,e in entityData.items():
    myString.write('\n<#char=%s,%s>\n' % (e['nif:beginIndex'],e['nif:endIndex']))
    myString.write('%30s\t%s ;\n'%('a','nif:RFC5147String, nif:Phrase, nif:OffsetBasedString') )
    myString.write(" ;\n".join([ '%30s\t%s'%(prop,myformat(val)) for prop,val in e.items() ])+' .\n')

  return(myString.getvalue())


if __name__ == '__main__':
  # assign input to variable
  try:
    myText = sys.argv[1]
    myServiceUrl = 'http://spotlight.dbpedia.org/rest/candidates'
    print(dbpediaspotlight2nif(myText, myServiceUrl))
  except:
    raise(Exception('a string is needed as input, none given'))
