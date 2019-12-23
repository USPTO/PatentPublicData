
# Patent Bulk Tools

{:with_toc_data}

#### Download
	--fetch-async [Boolean: download file async]  (default: false)                                                    
	--fetch-date [String: Single Date Range or list, example: 20150801-20150901,20160501-20160601]                               
	--fetch-delete [Boolean: Delete bulkfile after each processing download.] (default: false)                                          
	--fetch-filename [String: comma separated list of file names to match and download]                                       
	--fetch-limit [Integer: download file limit ; 0 is unlimited]  (default: 0)                                 
	--fetch-type <String: Patent Document Type [grant, application, gazette] ; type=? will show available types.>       
	--outDir [String: directory]  (default: download) 

    gov.uspto.bulkdata.cli.Fetch -f="." --fetch-type="grant" --fetch-date="20181101-20181115" --outDir="../download"

#### View
	-f, --file, --input <String: zip file, individual file or directory>
	--skip [Integer: records to skip]       (default: 0) 
	--limit [Integer: record limit]         (default: -1)   
	--format [String: Manually provide Patent Document Format]
	--id [String: Patent Id]                                    
	--num, --record-location [Integer: Record Number to retrive]                                 
	--out, --outfile, --output [String: out file]                                                  
	--type [String: types options: [raw,xml,json,json_flat,patft,object,text]] (default: object)

    gov.uspto.bulkdata.cli.View -f="../download/ipg181106.zip" --skip=961 --limit=1 --type="raw"

#### Extract
	gov.uspto.bulkdata.cli.View -f="../download/ipg181106.zip" --num=962 --type="raw" --out="./target/output/962.out"

#### Find
	-f, --file, --input <String: zip file, individual file or directory>
	--skip [Integer: records to skip]       (default: 0) 
	--limit [Integer: record limit]         (default: -1)   
	--format [String: Manually provide Patent Document Format]      
	-c, --count, --only-count [Boolean: Only output count of matching records]     (default: false)
	-m, --max, --max-count [Integer: Stop reading after total num record matches.]  (default: -1)       
	--xpath [String: XPath - XML node to perform match on]                                            
	-e, --regex, --regexp [String: Regex Pattern; multiple patterns are allowed with multiple occurance,  
	  flags 'i' for ignore-case and 'f'  for full-match; "'regex1~i','regex2~if' ]
	-v, --invert-match, --not [Boolean: Records NOT matching]    (default: false) 

    gov.uspto.bulkdata.cli.Grep -f="../download/ipg181106.zip" --xpath="//math" --matching-xml

#### XSLT
	gov.uspto.bulkdata.cli.Xslt -f="../download/ipg181106.zip"  --skip=0 --limit=1 --xslt="example.xslt" --prettyPrint=true

#### Transform (Read, Normalize, Transform)
	-f, --file, --input <String: zip file, individual file or directory>
	--skip [Integer: records to skip]       (default: 0) 
	--limit [Integer: record limit]         (default: -1)   
	--format [String: Manually provide Patent Document Format]
	--type [String: types options: [raw,json,json_flat,patft,solr,object,text]]
	--outDir <String: Output Directory> 
	--outBulk, --outputBulkFile [Boolean: Output bulk file, single file record per line] (default: true)
	--bulkKV, --bulkkv, --kv [Boolean: Prepend each record with docid ; DOC_ID<TAB>RECORD] (default: false) 
 
    gov.uspto.bulkdata.cli.Transformer -f="../download/ipg181106.zip" --type="json_flat" --outDir="./target/output" --outBulk=true --kv=true

#### Download, Transform, Delete
	Uses command line options for Fetch, Transform and Grep tools.

    gov.uspto.bulkdata.cli.Fetch -f="." --fetch-type="grant" --fetch-date="20181101-20181115" --outDir="./target/output" --outBulk=true --bulkKV=true --fetch-delete=true --transform 

#### Download, Transform, Grep, Delete (Corpus Builder)
	Uses command line options for Fetch, Transform and Grep tools.

    gov.uspto.bulkdata.cli.Fetch -f="." --fetch-type="grant" --fetch-date="20181101-20181115" --outDir="./target/output" --fetch-delete=true --bulkKV=true --outBulk=true --xpath="//invention-title[starts-with(text(), 'Food')]"

#### List of Useful Greps

	Text Regex Search
	--regex="<br/>"
	--regex="classification-cpc-text"
	
	Only Display Matching Record Count
	--regex="\D\D09856571" --count
	
	Only Display Matching Record iteration/locations
	--regex="\D\D09856571" --matching-record-location
	
	Only Display Matching Record
	--regex="\D\D09856571" --matching-record --max-count=1
	
	Parse XML using Xpath and Regex
	--xpath="//doc-number/text()" --regex="D0806350" --max-count=1
	
	--xpath="//description/p[contains(descendant-or-self::text(),'computer')]"
	
	--xpath="//description/descendant::text()" --regex="computer"
	--xpath="//p/descendant::text()" --regex="computer"
	--xpath="//table/descendant::text()" --regex="[Tt]omato"
	--xpath="//p/descendant::text()|//table/descendant::text()" --regex="[Tt]omato"\
	--xpath="//classification-cpc-text/text()" --regex="^A21C"
	
	--xpath="//invention-title[starts-with(text(), 'Food')]" --matching-xml
	--xpath="//invention-title[contains(text(), 'Food')]" --matching-xml
	--xpath="//invention-title/text()" --regex="Food" --matching-xml
	--xpath="//invention-title[@id='d2e53']/." --matching-xml
	
	Dump all:
	   Transitional phrases: --xpath="//description//text()" --regex="\b[A-Z][a-z ]{10,35}," --only-matching --no-source
	   Brace Codes: --regex="\{[A-z ]+ \(.+?\)\}" --only-matching --no-source
		  Company Names: --xpath="//orgname/text()" --no-source
		  Company Names with CountryCode 'DE': --xpath="//addressbook/orgname[../address/country[text()='DE']]/text()" --no-source
	   Last Names:  --xpath="//last-name/text()" --no-source
	   First Names: --xpath="//first-name/text()" --no-source
	   Trademarks: --xpath="//p/descendant::text()|//table/descendant::text()" --regex="\b((?:[a-z]-?)?[A-Z][\w'\/]{1,13}[\d\-_®™\/ ]*){1,5}\W?[®™]{1,2}" --only-matching --no-source
	   NPL Patent/Application Citations: --xpath="//nplcit/othercit/text()" --regex="\b([Pp]at\.|[P]atent\b|[Aa]pp\.|[Aa]pplication\b|PCT)" --no-source 
	
	Full XPath Lookup
	--xpath="//document-id/*[text() = 'D0806350']" --max-count=1
	--xpath="//document-id/*[contains(text(),'D0806350')]" --max-count=1
	--xpath="count(//claim-text/*[contains(text(),' consisting ')]) > 3" --matching-record

