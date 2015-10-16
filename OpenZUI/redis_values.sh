#!/bin/bash

# topology parameters
redis-cli HSET topologies:vSeen name "vSeen"
redis-cli HSET topologies:vSeen description "blah blah"
redis-cli HSET topologies:vSeen rabbit:host "160.40.51.165"
redis-cli HSET topologies:vSeen rabbit:port 5672
redis-cli HSET topologies:vSeen rabbit:user "qadmin"
redis-cli HSET topologies:vSeen rabbit:passwd "vseen"
redis-cli HSET topologies:vSeen mongo:host "160.40.51.165"
redis-cli HSET topologies:vSeen mongo:port 27017
redis-cli HSET topologies:vSeen mongo:user "dbadmin"
redis-cli HSET topologies:vSeen mongo:passwd "vseen"

# nodes
redis-cli HSET topologies:vSeen node:FileCrawler "{'instances':1, 'threadspercore':0}"
redis-cli HSET topologies:vSeen node:DescriptorExtractor "{'instances':2, 'threadspercore':2}"
redis-cli HSET topologies:vSeen node:DescriptorSaver "{'instances':1, 'threadspercore':0}"
redis-cli HSET topologies:vSeen node:SearchBroker "{'instances':1, 'threadspercore':0}"

# requires
redis-cli HSET topologies:vSeen requires:DescriptorExtractor:extract_lbp 4
redis-cli HSET topologies:vSeen requires:DescriptorExtractor:extract_colors true
redis-cli HSET topologies:vSeen requires:DescriptorExtractor:extract_lbp_int true
redis-cli HSET topologies:vSeen requires:DescriptorExtractor:extract_shape true
redis-cli HSET topologies:vSeen requires:SearchBroker:mongo_database "vseen"
redis-cli HSET topologies:vSeen requires:DescriptorSaver:mongo_database "vseen"
redis-cli HSET topologies:vSeen requires:SearchBroker:mongo_collection_media "media"
redis-cli HSET topologies:vSeen requires:DescriptorSaver:mongo_collection_media "media"
redis-cli HSET topologies:vSeen requires:SearchBroker:mongo_collection_articoli "articoli"
redis-cli HSET topologies:vSeen requires:FileCrawler:imagedir "/home/lazar/vseen/masks"
redis-cli HSET topologies:vSeen requires:DescriptorExtractor:resize_query true
redis-cli HSET topologies:vSeen requires:SearchBroker:colordist L1
redis-cli HSET topologies:vSeen requires:SearchBroker:color_alpha 0.5

# reset
redis-cli HSET topologies:vSeen reset:DescriptorExtractor:0 true

# connections
redis-cli HSET topologies:vSeen connection:SearchBroker:gr.iti.versace.impl.SearchBrokerWorker:ep_to_dextr:DescriptorExtractor:gr.iti.versace.impl.DescrExtrWorker:ep_from "{'mapping': 0, 'queue_name': 'vSeen_IMG2DESCR'}"
redis-cli HSET topologies:vSeen connection:DescriptorExtractor:gr.iti.versace.impl.DescrExtrWorker:ep_to_search:SearchBroker:gr.iti.versace.impl.SearchBrokerWorker:ep_from_dextr "{'mapping': 0, 'queue_name': 'vSeen_DESCR2BROKER'}"
redis-cli HSET topologies:vSeen connection:SearchBroker:gr.iti.versace.impl.SearchBrokerWorker:ep_to_index:IndexCluster:gr.iti.versace.IndexCluster:ep_from "{'mapping': 0, 'queue_name': 'vSeen_BROKER2INDEX'}"
redis-cli HSET topologies:vSeen connection:IndexCluster:gr.iti.versace.IndexCluster:ep_to:SearchBroker:gr.iti.versace.impl.IndexListenerWorker:ep_from_index "{'mapping': 0, 'queue_name': 'vSeen_INDEX2BROKER'}"
redis-cli HSET topologies:vSeen connection:FileCrawler:gr.iti.versace.impl.FileCrawlWorker:ep_to:DescriptorExtractor:gr.iti.versace.impl.DescrExtrWorker:ep_from "{'mapping': 0, 'queue_name': 'vSeen_IMG2DESCR'}"
redis-cli HSET topologies:vSeen connection:DescriptorExtractor:gr.iti.versace.impl.DescrExtrWorker:ep_to_db:DescriptorSaver:gr.iti.versace.impl.DBWorker:ep_from "{'mapping': 0, 'queue_name': 'vSeen_DESCR2DB'}"

# graph object
redis-cli HSET topologies:vSeen graph_object "{...}"

#configuration object
redis-cli HSET topologies:vSeen conf_object "{...}"
{
	'service_id':
	{
		'server_id':
		{
			'instance_id': 0,
			'threadspercore': 2,
			'status': 'installed/running'
		},
		'server_id':
		{
			'instance_id': 1,
			'threadspercore': 0,
			'status': 'installed/running'
		}
	}
}



