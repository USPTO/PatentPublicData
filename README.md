
# Patent Public Bulk Files

This module provides the ability to download and work with weekly bulk Patent zip files, along with other external data resources.

Note this source code has not been fully vetted for a production enviroment. It has been developed for prototyping and exploring of patent data.

#### Changes allowed to Patents after being published
Bulk files are not updated once published, updates can be received by indexing additional supplimental files also made availble to the public.  The following are fields which may update after being published.

| field | description |
| :-------- | ------------|
| Related Ids | patent family may continue to grow after being published or granted; update through new patent which references prior patent |
| assignee | Update available <u>daily</u> within Patent Assignment XML Dump files |
| Classifications | Updates available <u>monthly</u> within Master Classification File Dump files |

