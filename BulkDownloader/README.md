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

### Example Usage

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
            

#### Extract Patent XML Documents, utilizing limit
     gov.uspto.bulkdata.cli.ExtractPatentXml --source="download/ipa150101.zip" --limit 5 --skip 0 --outDir="download"

#### View single Patent XML Document.
     gov.uspto.bulkdata.cli.Look --source="download/ipa150101.zip" --limit 5 --skip 5 --fields=id,title,family

#### Dump a single Patent XML Document by location in zipfile; the 3rd document:
     gov.uspto.bulkdata.cli.Look --source="download/ipa150305.zip" --num=3 --fields=xml --out=download/patent.xml

#### Dump a single Patent XML Document by ID (note it may be slow as it parse each document to check its id):
     gov.uspto.bulkdata.cli.Look --source="download/ipa150305.zip" --id=US3931903A1 --fields=xml --out=download/patent.xml
     # id requirements: country code, patent id without leading zero, and kind code.

## CPC Classification Scheme
#### Download CPC Clasification Scheme (which updates 1-2 times per month)
     gov.uspto.bulkdata.cli.Download --source cpc --limit 1 --outdir="../download"
     
## Other Datasources
#### FDA National Drug Code Database (NDC)
     gov.uspto.bulkdata.cli.Download --source fda --limit 1 --outdir="../download"

## Download Any File
     gov.uspto.bulkdata.cli.DownloadFile --url="http://opennlp.sourceforge.net/models-1.5/en-sent.bin" --dir="../download"
