package com.synapticloop.panl.server.handler.fielderiser.field.facet;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.fielderiser.field.BaseField;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.LpseTokeniser;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.server.handler.tokeniser.token.facet.OrFacetLpseToken;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.synapticloop.panl.server.handler.processor.Processor.*;
import static com.synapticloop.panl.server.handler.processor.Processor.FORWARD_SLASH;

public class PanlOrFacetField extends PanlFacetField {
	private static final Logger LOGGER = LoggerFactory.getLogger(PanlOrFacetField.class);

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	//                            OR Facet properties                          //
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	protected boolean isOrFacet = false;

	public PanlOrFacetField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, lpseLength);

		this.isOrFacet = properties.getProperty(PROPERTY_KEY_PANL_OR_FACET + lpseCode, "false").equalsIgnoreCase("true");
		if (this.isOrFacet) {
			String propertyFacetMinCount = properties.getProperty(PROPERTY_KEY_SOLR_FACET_MIN_COUNT, null);
			if (null != propertyFacetMinCount) {
				try {
					int minCount = Integer.parseInt(propertyFacetMinCount);
					if (minCount != 0) {
						getLogger().warn("Property '{}' __MUST__ be set to zero for '{}{}' to be enabled.", PROPERTY_KEY_SOLR_FACET_MIN_COUNT, PROPERTY_KEY_PANL_OR_FACET, lpseCode);
					}
				} catch (NumberFormatException e) {
					getLogger().error("Property '{}' __MUST__ be set.", PROPERTY_KEY_SOLR_FACET_MIN_COUNT);
					throw new PanlServerException("Property " + PROPERTY_KEY_SOLR_FACET_MIN_COUNT + " was not set.");
				}
			}
		}
	}

	@Override public List<String> explainAdditional() {
		return List.of();
	}


	@Override public Logger getLogger() {
		return (LOGGER);
	}

	@Override protected void applyToQueryInternal(SolrQuery solrQuery, List<LpseToken> lpseTokenList) {
		// if there is only one...
		if (lpseTokenList.size() == 1) {
			OrFacetLpseToken facetLpseToken = (OrFacetLpseToken) lpseTokenList.get(0);

			solrQuery.addFilterQuery(
					String.format("%s:\"%s\"",
							facetLpseToken.getSolrField(),
							facetLpseToken.getValue()));
			return;
		}

		StringBuilder stringBuilder = new StringBuilder();
		boolean isFirst = true;
		// at this point, we are going through the or filters
		for (LpseToken lpseToken : lpseTokenList) {
			OrFacetLpseToken orFacetLpseToken = (OrFacetLpseToken) lpseToken;
			if (isFirst) {
				stringBuilder
						.append(orFacetLpseToken.getSolrField())
						.append(":(");
			}

			if (!isFirst) {
				stringBuilder.append(" OR ");
			}

			stringBuilder
					.append("\"")
					.append(orFacetLpseToken.getValue())
					.append("\"");

			isFirst = false;
		}

		stringBuilder.append(")");
		solrQuery.addFilterQuery(stringBuilder.toString());
	}

	public static final String JSON_KEY_IS_OR_FACET = "is_or_facet";

	@Override public void appendToAvailableObjectInternal(JSONObject jsonObject) {
		jsonObject.put(JSON_KEY_IS_OR_FACET, true);
	}

	/**
	 * <p>OR facets are different, in that they will return a currently selected
	 * facet and more</p>
	 *
	 * @param facetObject The facet object to append to
	 * @param collectionProperties The colleciton properties
	 * @param panlTokenMap The incoming Panl tokens
	 * @param existingLpseValues The existing LPSE values
	 * @param facetCountValues The facet count values
	 * @param numFound Number of results found
	 * @param numFoundExact Whether the number of results were exact
	 *
	 * @return Whether any values were appended
	 */
	@Override public boolean appendAvailableValues(
			JSONObject facetObject,
			CollectionProperties collectionProperties,
			Map<String, List<LpseToken>> panlTokenMap,
			Set<String> existingLpseValues,
			List<FacetField.Count> facetCountValues,
			long numFound,
			boolean numFoundExact) {

		JSONArray facetValueArrays = new JSONArray();

		// if we currently have the facet value, don't do anything
		Set<String> currentValueSet = new HashSet<>();
		for (LpseToken lpseToken : panlTokenMap.getOrDefault(this.lpseCode, new ArrayList<>())) {
			currentValueSet.add(lpseToken.getValue());
		}


		for (FacetField.Count value : facetCountValues) {
			boolean shouldAdd = true;
			// at this point - we need to see whether we already have the 'value'
			// as a facet - as there is no need to have it again

			String valueName = value.getName();

			if (currentValueSet.contains(valueName)) {
				continue;
			}

			// also, if the count of the number of found results is the same as
			// the number of the count of the facet - then we may not need to
			// include it
			if (!panlIncludeSameNumberFacets &&
					numFound == value.getCount() &&
					numFoundExact) {
				shouldAdd = false;
			}

			if (shouldAdd) {
				JSONObject facetValueObject = new JSONObject();
				facetValueObject.put(JSON_KEY_VALUE, valueName);
				facetValueObject.put(JSON_KEY_COUNT, value.getCount());
				facetValueObject.put(JSON_KEY_ENCODED, getEncodedPanlValue(valueName));
				facetValueArrays.put(facetValueObject);
			}
		}

		// if we don't have any values for this facet, don't put it in

		if (!facetValueArrays.isEmpty()) {
			facetObject.put(JSON_KEY_VALUES, facetValueArrays);
			if (null != lpseCode) {
				facetObject.put(JSON_KEY_URIS,
						getAdditionURIObject(
								collectionProperties,
								this,
								panlTokenMap));
				return (true);
			}
		}
		return (false);
	}

	/**
	 * <p>This is an OR facet, so we can additional </p>
	 *
	 * @param collectionProperties The collection properties
	 * @param lpseField The LPSE field that this applies to
	 * @param panlTokenMap The inbound Panl tokens
	 *
	 * @return The JSON object with the URIs for adding this field to the
	 * 		existing search URI.
	 */
	@Override
	protected JSONObject getAdditionURIObject(
			CollectionProperties collectionProperties,
			BaseField lpseField,
			Map<String, List<LpseToken>> panlTokenMap) {

		JSONObject additionObject = new JSONObject();

		StringBuilder lpseUri = new StringBuilder(FORWARD_SLASH);
		StringBuilder lpseUriBefore = new StringBuilder();
		StringBuilder lpseUriCode = new StringBuilder();

		for (BaseField baseField : collectionProperties.getLpseFields()) {
			// we need to add in any other token values in the correct order
			String orderedLpseCode = baseField.getLpseCode();

			if (orderedLpseCode.equals(this.lpseCode)) {
				// we have found the current LPSE code, so reset the URI and add it to
				// the after

				lpseUri.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				lpseUriBefore.append(lpseUri);
				lpseUri.setLength(0);
				lpseUriCode.append(baseField.getLpseCode(panlTokenMap, collectionProperties));
				lpseUriCode.append(this.lpseCode);

			} else {
				// if we don't have a current token, just carry on
				if (!panlTokenMap.containsKey(orderedLpseCode)) {
					continue;
				}
				lpseUri.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				lpseUriCode.append(baseField.getLpseCode());
			}
		}

		additionObject.put(JSON_KEY_BEFORE, lpseUriBefore.toString());

		additionObject.put(JSON_KEY_AFTER, FORWARD_SLASH + lpseUri.toString() + lpseUriCode.toString() + FORWARD_SLASH);
		return (additionObject);
	}

	@Override
	public LpseToken instantiateToken(CollectionProperties collectionProperties, String lpseCode, String query, StringTokenizer valueTokeniser, LpseTokeniser lpseTokeniser) {
		return (new OrFacetLpseToken(collectionProperties, this.lpseCode, lpseTokeniser, valueTokeniser));
	}

	@Override public void addToRemoveObject(JSONObject removeObject, LpseToken lpseToken) {
		removeObject.put(JSON_KEY_IS_OR_FACET, true);
	}
}
