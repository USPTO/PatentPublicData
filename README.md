
# Patent Public Bulk Files

Tool kit to download, read and utilize open patent data provided to the public.

## Notice
This source code is a work in progress and has not been fully vetted for a production environment. 

## Two main modules
<ul>
<li><b><a href="https://github.com/USPTO/PatentPublicData/tree/master/BulkDownloader">Bulk Downloader</a></b> automates downloading of public bulk patent data</li>
<li><b><a href="https://github.com/USPTO/PatentPublicData/tree/master/PatentDocument">Patent Document</a></b> provides the ability to iterate and read patents directly from the large bulk download files, supports reading patent documents from 1976 to current (formats: Greenbook, SGML, PAP, Redbook XML) into a normalized Patent Object Model.</li>
</ul>

## Features
<ul>
<li><b>Download</b> Bulk Patent Grants and Applications, as well as additional resources</li>
<li><b>View</b> individual Patent Documents directly from the large bulk files</li>
<li><b>Read</b> Patent Documents directly from the large bulk files, supports reading patent documents from 1976 to current (formats: Greenbook, SGML, PAP, Redbook XML) into a normalized Patent Object Model</li>
<li><b>Extract</b> Patent Documents from bulk files</li>
<li><b>Normalize and transform</b> Patent data before loading into a data resource</li>
<li><b>Patent Claim analysis</b> facilitated by generated claim tree</li>
<li><b>Update Classifications</b> from <b><a href="https://bulkdata.uspto.gov/data2/patent/classification/cpc/">Master CPC File</a></b> (contains current CPC classification for patents starting a patent number 1)
<li><b>Include CPC definitions</b> from <a href="http://www.cooperativepatentclassification.org/cpcSchemeAndDefinitions/Bulk.html">CPC Scheme</a></b></li>
<li><b>Build a corpus</b> using Corpus Builder which automates building a corpus by downloading and extracting patent/applications matching specified classifications, one bulk file at a time for a date range.</li>
</ul>

## Changes after Patents are published
Bulk files are not updated once published, updates can be received by indexing additional supplemental files also made available to the public. The following are fields which may update after being published.

| field | description |
| :-------- | ------------|
| Related Ids | patent family may continue to grow after being published or granted; update through new patent which references prior patent |
| assignee | Update available <u>daily</u> within Patent Assignment XML Dump files |
| Classifications | Updates available <u>monthly</u> within Master Classification File Dump files |


## Other Information
The United States Department of Commerce (DOC)and the United States Patent and Trademark Office (USPTO) GitHub project code is provided on an ‘as is’ basis without any warranty of any kind, either expressed, implied or statutory, including but not limited to any warranty that the subject software will conform to specifications, any implied warranties of merchantability, fitness for a particular purpose, or freedom from infringement, or any warranty that the documentation, if provided, will conform to the subject software.  DOC and USPTO disclaim all warranties and liabilities regarding third party software, if present in the original software, and distribute it as is.  The user or recipient assumes responsibility for its use. DOC and USPTO have relinquished control of the information and no longer have responsibility to protect the integrity, confidentiality, or availability of the information. 

User and recipient agree to waive any and all claims against the United States Government, its contractors and subcontractors as well as any prior recipient, if any.  If user or recipient’s use of the subject software results in any liabilities, demands, damages, expenses or losses arising from such use, including any damages from products based on, or resulting from recipient’s use of the subject software, user or recipient shall indemnify and hold harmless the United States government, its contractors and subcontractors as well as any prior recipient, if any, to the extent permitted by law.  User or recipient’s sole remedy for any such matter shall be immediate termination of the agreement.  This agreement shall be subject to United States federal law for all purposes including but not limited to the validity of the readme or license files, the meaning of the provisions and rights and the obligations and remedies of the parties. Any claims against DOC or USPTO stemming from the use of its GitHub project will be governed by all applicable Federal law. “User” or “Recipient” means anyone who acquires or utilizes the subject code, including all contributors. “Contributors” means any entity that makes a modification. 

This agreement or any reference to specific commercial products, processes, or services by service mark, trademark, manufacturer, or otherwise, does not in any manner constitute or imply their endorsement, recommendation or favoring by DOC or the USPTO, nor does it constitute an endorsement by DOC or USPTO or any prior recipient of any results, resulting designs, hardware, software products or any other applications resulting from the use of the subject software.  The Department of Commerce seal and logo, or the seal and logo of a DOC bureau, including USPTO, shall not be used in any manner to imply endorsement of any commercial product or activity by DOC, USPTO  or the United States Government.

<br />
<br />
<p xmlns:dct="http://purl.org/dc/terms/" xmlns:vcard="http://www.w3.org/2001/vcard-rdf/3.0#">
  <a rel="license"
     href="http://creativecommons.org/publicdomain/zero/1.0/">
    <img src="http://i.creativecommons.org/p/zero/1.0/88x31.png" style="border-style: none;" alt="CC0" />
  </a>
  <br />
  To the extent possible under law,
  <a rel="dct:publisher"
     href="https://github.com/USPTO/PatentPublicData">https://github.com/USPTO/PatentPublicData</a>
  has waived all copyright and related or neighboring rights to
  <span property="dct:title">Patent Public Data</span>.
This work is published from:
<span property="vcard:Country" datatype="dct:ISO3166"
      content="US" about="https://github.com/USPTO/PatentPublicData">
  United States</span>.
</p>
