package com.synapticloop.panl.server.handler.properties.holder;

/*
 * Copyright (c) 2008-2025 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

import com.synapticloop.panl.util.Constants;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.Properties;

/**
 * <p>This holds all 'More Like This' properties and can be used to apply the
 * parameters to the query.</p>
 *
 * @author Synapticloop
 */

public class MltHolder {
	private boolean mltEnabled = false;
	private String mltHandler = Constants.DEFAULT_MLT_HANDLER;

	private String mltFieldList;
	private String panlMltMintf;
	private String panlMltMindf;
	private String panlMltMaxdf;
	private String panlMltMaxdfpct;
	private String panlMltMinwl;
	private String panlMltMaxwl;
	private String panlMltMaxqt;
	private String panlMltMaxntp;

	// (Optional - Default: false) Specifies if the query will be boosted by the interesting term
	// relevance. Possible values are true or false.
	private boolean mltBoost = false;
	private String panlMltQf;


	private String panlMltInterestingTerms;
	private String panlMltMatchInclude;
	private String panlMltMatchOffset;

	public MltHolder(Properties properties) {
		this.mltEnabled = properties.getProperty(Constants.Property.Panl.PANL_MLT_ENABLE,
				Constants.BOOLEAN_FALSE_VALUE).equals(Constants.BOOLEAN_TRUE_VALUE);

		if(!this.mltEnabled) {
			return;
		}

		// at this point MLT is enabled so we should load the additional properties

		this.mltFieldList = properties.getProperty(Constants.Property.Panl.PANL_MLT_FL, null);
		// if the above is null - error
		this.panlMltMintf = properties.getProperty(Constants.Property.Panl.PANL_MLT_MINTF, "2");
		this.panlMltMindf = properties.getProperty(Constants.Property.Panl.PANL_MLT_MINDF);
		this.panlMltMaxdf = properties.getProperty(Constants.Property.Panl.PANL_MLT_MAXDF);
		this.panlMltMaxdfpct = properties.getProperty(Constants.Property.Panl.PANL_MLT_MAXDFPCT);
		this.panlMltMinwl = properties.getProperty(Constants.Property.Panl.PANL_MLT_MINWL);
		this.panlMltMaxwl = properties.getProperty(Constants.Property.Panl.PANL_MLT_MAXWL);
		this.panlMltMaxqt = properties.getProperty(Constants.Property.Panl.PANL_MLT_MAXQT);
		this.panlMltMaxntp = properties.getProperty(Constants.Property.Panl.PANL_MLT_MAXNTP);

		this.mltBoost = properties.getProperty(Constants.Property.Panl.PANL_MLT_BOOST,
				Constants.BOOLEAN_FALSE_VALUE).equals(Constants.BOOLEAN_TRUE_VALUE);

		this.panlMltQf = properties.getProperty(Constants.Property.Panl.PANL_MLT_QF);
		this.panlMltInterestingTerms = properties.getProperty(Constants.Property.Panl.PANL_MLT_INTERESTINGTERMS);
		this.panlMltMatchInclude = properties.getProperty(Constants.Property.Panl.PANL_MLT_MATCH_INCLUDE);
		this.panlMltMatchOffset = properties.getProperty(Constants.Property.Panl.PANL_MLT_MATCH_OFFSET);
	}

	public void applyMltToQuery(SolrQuery solrQuery) {
		if(!this.mltEnabled) {
			return;
		}

		solrQuery.setRequestHandler(this.mltHandler);
		solrQuery.setMoreLikeThis(true);
		solrQuery.setMoreLikeThisBoost(this.mltBoost);
	}

	public boolean getIsMltEnabled() {
		return(this.mltEnabled);
	}

	public String getMltHandler() {
		return(this.mltHandler);
	}
}
