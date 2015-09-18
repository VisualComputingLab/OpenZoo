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
redis-cli HSET topologies:vSeen node:FileCrawler "{'instances':1, 'workerspercore':0}"
redis-cli HSET topologies:vSeen node:DescriptorExtractor "{'instances':2, 'workerspercore':2}"
redis-cli HSET topologies:vSeen node:DescriptorSaver "{'instances':1, 'workerspercore':0}"
redis-cli HSET topologies:vSeen node:SearchBroker "{'instances':1, 'workerspercore':0}"

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
	service_id:
	{

	}
}


# full object (not needed)
redis-cli HSET vSeen full_object "{'topologyName':'vSeen','topologyNodes':[{'name':'FileCrawler.war','instances':'1','workerspercore':'0','id':'FileCrawler'},{'name':'DescriptorExtractor.war','instances':'2','workerspercore':'2','id':'DescriptorExtractor'},{'name':'DescriptorSaver.war','instances':'1','workerspercore':'0','id':'DescriptorSaver'},{'name':'SearchBroker.war','instances':'1','workerspercore':'0','id':'SearchBroker'}],'topologyConnections':[{'conn_name':'con_82','conn_id':'con_82_FileCrawler_DescriptorExtractor','source_node':'FileCrawler','target_node':'DescriptorExtractor','source_endpoint':'gr.iti.versace.impl.FileCrawlWorker:ep_to','target_endpoint':'gr.iti.versace.impl.DescrExtrWorker:ep_from','conn_mapping':'Available','routing_keys':'-'},{'conn_name':'con_87','conn_id':'con_87_DescriptorExtractor_DescriptorSaver','source_node':'DescriptorExtractor','target_node':'DescriptorSaver','source_endpoint':'gr.iti.versace.impl.DescrExtrWorker:ep_to','target_endpoint':'gr.iti.versace.impl.DBWorker:ep_from','conn_mapping':'Available','routing_keys':'-'},{'conn_name':'con_93','conn_id':'con_93_SearchBroker_DescriptorExtractor','source_node':'SearchBroker','target_node':'DescriptorExtractor','source_endpoint':'gr.iti.versace.impl.SearchBrokerWorker:ep_to_dextr','target_endpoint':'gr.iti.versace.impl.DescrExtrWorker:ep_from','conn_mapping':'Available','routing_keys':'-'},{'conn_name':'con_95','conn_id':'con_95_SearchBroker_IndexCluster','source_node':'SearchBroker','target_node':'IndexCluster','source_endpoint':'gr.iti.versace.impl.redis-cli SearchBrokerWorker:ep_to_index','target_endpoint':'gr.iti.versace.IndexCluster:ep_from','conn_mapping':'Available','routing_keys':'-'}]}"

{
	'topologyName':'vSeen',
	'topologyNodes':
	[
		{
			'name':'FileCrawler.war',
			'instances':'1',
			'workerspercore':'0',
			'id':'FileCrawler'
		},
		{
			'name':'DescriptorExtractor.war',
			'instances':'2',
			'workerspercore':'2',
			'id':'DescriptorExtractor'
		},
		{
			'name':'DescriptorSaver.war',
			'instances':'1',
			'workerspercore':'0',
			'id':'DescriptorSaver'
		},
		{
			'name':'SearchBroker.war',
			'instances':'1',
			'workerspercore':'0',
			'id':'SearchBroker'
		}
	],
	'topologyConnections':
	[
		{
			'conn_name':'con_82',
			'conn_id':'con_82_FileCrawler_DescriptorExtractor',
			'source_node':'FileCrawler',
			'target_node':'DescriptorExtractor',
			'source_endpoint':'gr.iti.versace.impl.FileCrawlWorker:ep_to',
			'target_endpoint':'gr.iti.versace.impl.DescrExtrWorker:ep_from',
			'conn_mapping':'Available',
			'routing_keys':'-'
		},
		{
			'conn_name':'con_87',
			'conn_id':'con_87_DescriptorExtractor_DescriptorSaver',
			'source_node':'DescriptorExtractor',
			'target_node':'DescriptorSaver',
			'source_endpoint':'gr.iti.versace.impl.DescrExtrWorker:ep_to',
			'target_endpoint':'gr.iti.versace.impl.DBWorker:ep_from',
			'conn_mapping':'Available',
			'routing_keys':'-'
		},
		{
			'conn_name':'con_93',
			'conn_id':'con_93_SearchBroker_DescriptorExtractor',
			'source_node':'SearchBroker',
			'target_node':'DescriptorExtractor',
			'source_endpoint':'gr.iti.versace.impl.SearchBrokerWorker:ep_to_dextr',
			'target_endpoint':'gr.iti.versace.impl.DescrExtrWorker:ep_from',
			'conn_mapping':'Available',
			'routing_keys':'-'
		},
		{
			'conn_name':'con_95',
			'conn_id':'con_95_SearchBroker_IndexCluster',
			'source_node':'SearchBroker',
			'target_node':'IndexCluster',
			'source_endpoint':'gr.iti.versace.impl.SearchBrokerWorker:ep_to_index',
			'target_endpoint':'gr.iti.versace.IndexCluster:ep_from',
			'conn_mapping':'Available',
			'routing_keys':'-'
		}
	]
}
