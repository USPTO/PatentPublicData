# Bulk Download

This module provides the ability to download and work with weekly bulk Patent zip files, along with other external data resources.

### Features
<ul>
<li>Async Downloads</li>
<li>Automatic retry on failure</li>
<li>Restartable (currently only syncronous downloads)</li>
</ul>

### Sources
<ul>
<li>USPTO Bulk Patent Download, grants and applications from Google or Reedtech</li>
<li>Patent CPC Classification Scheme</li>
<li>FDA "NDA" Drug Database</li>
</ul>
Additional sources can be added to sources.xml

## Download Bulk Patent Zips from USPTO
  Downloads from https://bulkdata.uspto.gov/
    
      gov.uspto.bulkdata.cli2.BulkData

        options:
          --type=application               Data type: [grant, application, gazette]
          --date="20140101-20161231"       Single date range or comma seperated list of date ranges
          --limit=0
          --skip=0
          --outdir="../download"
          --async=false
          --filename="ipa140109.zip"

## Download other External Resources
     gov.uspto.bulkdata.cli.Download

        Options:
          --available             Display available sources
          --source=cpc            Source provider: [cpc, fda, reetech, google]
          --type=cpc_scheme       Data type: [cpc_scheme, nda, patent_grant, patent_application]
          --limit=1
          --skip=0
          --outdir="../download"
          --async=false
          --filename="ipa140109.zip"
            
## Extract Patent Documents
     gov.uspto.bulkdata.cli.ExtractPatent --source="download/ipa150101.zip" --skip 0 --limit 5 --outdir="download" --aps=false

## View single Patent Document
     gov.uspto.bulkdata.cli.Look

       Options:
          --source="download/ipa150101.zip"
          --skip=0                  
          --limit=1                 
          --num=100                    Diplay by iteration number in bulk file    
          --id=US3931903A1             Display by Patent ID
          --fields=id,title,family     Fields to display
          --out=download/patent.xml    Output to File instead of STDOUT
          --aps=true                   Viewing a Greenbook Patent
          
       Fields:
          raw        Display raw Document
          object     Display Patent toString()
          id
          title
          abstract
          description
          citations
          claims
          assignee
          inventor
          classification
          family
        
##### Dump a single Patent XML Document by location in zipfile; the 3rd document:
     gov.uspto.bulkdata.cli.Look --source="download/ipa150305.zip" --num=3 --fields=xml --out=download/patent.xml

##### Dump a single Patent XML Document by ID (note it may be slow as it parse each document to check its id):
     gov.uspto.bulkdata.cli.Look --source="download/ipa150305.zip" --id=US3931903A1 --fields=xml --out=download/patent.xml
     # id requirements: country code, patent id without leading zero, and kind code.
     
