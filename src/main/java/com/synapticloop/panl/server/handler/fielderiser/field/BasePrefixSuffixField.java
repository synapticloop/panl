package com.synapticloop.panl.server.handler.fielderiser.field;

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

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.properties.CollectionProperties;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import com.synapticloop.panl.util.URLHelper;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.synapticloop.panl.server.handler.processor.Processor.*;
import static com.synapticloop.panl.server.handler.processor.Processor.FORWARD_SLASH;

/**
 * <p>The abstract <code>BasePrefixSuffixField</code> is the parent object for
 * any parameter, operand, or facet that allows a prefix and/or suffix.</p>
 */
public abstract class BasePrefixSuffixField extends BaseField {
	public static final String PROPERTY_KEY_SUFFIX_PREFIX = ".prefix";
	public static final String PROPERTY_KEY_SUFFIX_SUFFIX = ".suffix";

	protected boolean hasValuePrefix = false;
	protected boolean hasValueSuffix = false;
	protected String valuePrefix;
	protected String valueSuffix;

	/**
	 * <p>Instantiate the BasePrefixSuffixField</p>
	 *
	 * @param lpseCode The LPSE code that this is bound to
	 * @param propertyKey The property key from the properties file
	 * @param properties The properties to look up
	 * @param solrCollection The Solr collection that this will connect to (this
	 * 		is used for debugging and logging output)
	 * @param panlCollectionUri The Panl collection URI that this is bound to
	 * 		(this is used for debugging and logging output)
	 * @param lpseLength The length of the LPSE code
	 *
	 * @throws PanlServerException If there was an error parsing/decoding the
	 * 		token
	 */
	public BasePrefixSuffixField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri, int lpseLength) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, lpseLength);

		populateSuffixAndPrefix();
	}

	/**
	 * <p>Instantiate the BasePrefixSuffixField</p>
	 *
	 * @param lpseCode The LPSE code that this is bound to
	 * @param propertyKey The property key from the properties file
	 * @param properties The properties to look up
	 * @param solrCollection The Solr collection that this will connect to (this
	 * 		is used for debugging and logging output)
	 * @param panlCollectionUri The Panl collection URI that this is bound to
	 * 		(this is used for debugging and logging output)
	 *
	 * @throws PanlServerException If there was an error parsing/decoding the
	 * 		token
	 */
	public BasePrefixSuffixField(String lpseCode, String propertyKey, Properties properties, String solrCollection, String panlCollectionUri) throws PanlServerException {
		super(lpseCode, propertyKey, properties, solrCollection, panlCollectionUri, 1);

		populateParamSuffixAndPrefix(propertyKey);
	}

	/**
	 * <p>Populate the prefix and suffix for a parameter LPSE code from the
	 * properties file if they exist.</p>
	 *
	 * @param propertyKey The property key to look up
	 */
	protected void populateParamSuffixAndPrefix(String propertyKey) {
		this.valuePrefix = properties.getProperty(propertyKey + PROPERTY_KEY_SUFFIX_PREFIX);

		this.valueSuffix = properties.getProperty(propertyKey + PROPERTY_KEY_SUFFIX_SUFFIX);

		checkPrefixSuffix();
	}

	/**
	 * <p>Populate the prefix and suffix for a facet field from the properties
	 * file if they exist.</p>
	 */
	protected void populateSuffixAndPrefix() {
		this.valuePrefix = properties.getProperty(PROPERTY_KEY_PANL_PREFIX + lpseCode);

		this.valueSuffix = properties.getProperty(PROPERTY_KEY_PANL_SUFFIX + lpseCode);

		checkPrefixSuffix();
	}

	/**
	 * <p>Check the prefix and suffix value to determine whether they exist.  If
	 * they are null, whitespace only or an empty string then they will be set as
	 * not having a prefix, or suffix respectively.</p>
	 */
	private void checkPrefixSuffix() {
		if (this.valuePrefix != null && !this.valuePrefix.isEmpty()) {
			hasValuePrefix = true;
		}

		if (this.valueSuffix != null && !this.valueSuffix.isEmpty()) {
			hasValueSuffix = true;
		}
	}

	/**
	 * <p>Get the value prefix for this field, or an empty string if it does not
	 * exist.</p>
	 *
	 * @return The value prefix
	 */
	public String getValuePrefix() {
		if (hasValuePrefix) {
			return (valuePrefix);
		} else {
			return ("");
		}
	}

	/**
	 * <p>Get the value suffix for this field, or an empty string if it does not
	 * exist.</p>
	 *
	 * @return The value suffix
	 */
	public String getValueSuffix() {
		if (hasValueSuffix) {
			return (valueSuffix);
		} else {
			return ("");
		}
	}

	/**
	 * <p>The panl value (from the URI) can have a prefix or suffix, or both
	 * applied to it.</p>
	 *
	 * <p>Remove any suffixes, or prefixes from a URI parameter, should they be
	 * defined for the LPSE code.</p>
	 *
	 * <p>Additionally, if this is a boolean field, it may be that there also is
	 * a replacement for true/false for it.</p>
	 *
	 * <p>This will also validate the value by the Solr field type, at the
	 * moment, the only fields that have additional validation are:</p>
	 *
	 * <ul>
	 *   <li>"solr.IntPointField"</li>
	 *   <li>"solr.LongPointField"</li>
	 *   <li>"solr.DoublePointField"</li>
	 *   <li>"solr.FloatPointField"</li>
	 * </ul>
	 *
	 * @param value the value to convert if any conversions are required
	 *
	 * @return the de-suffixed, de-prefixed, and de-replaced value.  This will
	 * 		return <code>null</code> if it is invalid.
	 */
	@Override public String getDecodedValue(String value) {
		if (null == value) {
			return (null);
		}

		String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);

		if (hasValuePrefix) {
			if (decodedValue.startsWith(valuePrefix)) {
				decodedValue = decodedValue.substring(valuePrefix.length());
			} else {
				return (null);
			}
		}

		if (hasValueSuffix) {
			if (decodedValue.endsWith(valueSuffix)) {
				decodedValue = decodedValue.substring(0, decodedValue.length() - valueSuffix.length());
			} else {
				return (null);
			}
		}

		// now we are going to validate the fields, boolean and string fields have
		// their own validation
		String validatedValue = getValidatedValue(decodedValue);
		if (null != validatedValue && validatedValue.isBlank()) {
			return (null);
		} else {
			return validatedValue;
		}
	}

	/**
	 * <p>Get the encoded URL encoded Panl value which will add a prefix, suffix,
	 * both or neither, and then URL encode it.</p>
	 *
	 * @param value The value
	 *
	 * @return The URL encoded Panl value
	 */
	@Override public String getEncodedPanlValue(String value) {
		StringBuilder sb = new StringBuilder();

		if (hasValuePrefix) {
			sb.append(valuePrefix);
		}

		sb.append(value);

		if (hasValueSuffix) {
			sb.append(valueSuffix);
		}

		return (URLHelper.encodeURIPath(sb.toString()));
	}

	/**
	 * <p>Get the encoded URL encoded Panl value which will add a prefix, suffix,
	 * both or neither, and then URL encode it.</p>
	 *
	 * @param lpseToken The incoming token
	 *
	 * @return The URL encoded Panl value
	 */
	@Override public String getEncodedPanlValue(LpseToken lpseToken) {
		if (null == lpseToken.getValue()) {
			return ("");
		} else {
			return (getEncodedPanlValue(lpseToken.getValue()));
		}
	}

	/**
	 * <p>Append any keys to the available JSON response object.  This method
	 * does not add any additional keys.</p>
	 *
	 * @param jsonObject The JSON object to append to
	 */
	@Override protected void appendToAvailableObjectInternal(JSONObject jsonObject) {
		if (null != valueSeparator) {
			jsonObject.put(JSON_KEY_VALUE_SEPARATOR, valueSeparator);
		}
	}

	/**
	 * <p>Add additional detail to the explanation, this is used for debugging
	 * and informational purposes through the Panl viewer explainer web app.</p>
	 *
	 * @return The list of additional explanations for this object
	 */
	@Override public List<String> explainAdditional() {
		List<String> explanations = new ArrayList<>();
		if (hasValuePrefix) {
			explanations.add("Has a prefix of '" + getValuePrefix() + "' which will be prepended to the value in the URI path.");
		} else {
			explanations.add("Has no prefix.");
		}

		if (hasValueSuffix) {
			explanations.add("Has a suffix of '" + getValueSuffix() + "' which will be appended to the value in the URI path.");
		} else {
			explanations.add("Has no suffix.");
		}

		return (explanations);
	}

	@Override
	protected JSONObject getAdditionURIObject(
			CollectionProperties collectionProperties,
			BaseField lpseField,
			Map<String, List<LpseToken>> panlTokenMap) {

		JSONObject additionObject = new JSONObject();

		StringBuilder lpseUri = new StringBuilder(FORWARD_SLASH);
		StringBuilder lpseUriBefore = new StringBuilder();
		StringBuilder lpseUriCode = new StringBuilder();

		Map<String, Boolean> lpseCodeMap = new HashMap<>();

		for(BaseField baseField : collectionProperties.getLpseFields()) {
			// we need to add in any other token values in the correct order
			String orderedLpseCode = baseField.getLpseCode();

			if (orderedLpseCode.equals(this.lpseCode)) {
				// we have found the current LPSE code, so reset the URI and add it to
				// the after
				if (valueSeparator != null) {
					lpseUri.append(getMultivalueURIPathStart(panlTokenMap));
				} else {
					lpseUri.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				}

				lpseUriBefore.append(lpseUri);
				lpseUri.setLength(0);
				if (valueSeparator != null) {
					lpseUri.append(getMultiValueURIPathEnd());
					lpseUri.append("/");
				}

				if(collectionProperties.getIsMultiValuedSeparatorFacetField(this.lpseCode)) {
					if (!lpseCodeMap.containsKey(this.lpseCode)) {
						lpseUriCode.append(this.lpseCode);
					}
					lpseCodeMap.put(this.lpseCode, true);
				} else {
					lpseUriCode.append(baseField.getLpseCode(panlTokenMap, collectionProperties));
					lpseUriCode.append(this.lpseCode);
				}

			} else {
				// if we don't have a current token, just carry on
				if (!panlTokenMap.containsKey(orderedLpseCode)) {
					continue;
				}

				// normally
				lpseUri.append(baseField.getURIPath(panlTokenMap, collectionProperties));
				int numTokens = panlTokenMap.get(orderedLpseCode).size();
				if (numTokens == 1) {
					// if we have a range facet - we need to make sure that we are
					// encoding it correctly there can only be one range token for the
					// panl field (no over-lapping ranges, or distinct ranges)

					// if it is not a range facet - then this won't do any harm and is a
					// better implementation
					lpseUriCode.append(baseField.getLpseCode(panlTokenMap.get(orderedLpseCode).get(0), collectionProperties));
				} else {

					// check for or separators...
					if(collectionProperties.getIsMultiValuedSeparatorFacetField(baseField.getLpseCode())) {
						lpseUriCode.append(baseField.getLpseCode());
					} else {
						// just replace it with the correct number of LPSE codes
						if(!baseField.getHasURIComponent()) {
							lpseUriCode.append(baseField.getResetLpseCode(panlTokenMap, collectionProperties));
						} else {
							lpseUriCode.append(new String(new char[numTokens]).replace("\0", baseField.getLpseCode()));
						}
					}
				}
			}
		}

		additionObject.put(JSON_KEY_BEFORE, lpseUriBefore.toString());

		// if we have an or separator, we have already added the forward slash
		if (valueSeparator != null) {
			additionObject.put(JSON_KEY_AFTER, lpseUri.toString() + lpseUriCode + FORWARD_SLASH);
		} else {
			additionObject.put(JSON_KEY_AFTER, FORWARD_SLASH + lpseUri + lpseUriCode + FORWARD_SLASH);
		}

		return (additionObject);
	}

	/**
	 * <p>Get the Start of the OR URI path for this field</p>
	 *
	 * @param panlTokenMap The token map with the LPSe codes and values
	 *
	 * @return The URI path
	 */
	private String getMultivalueURIPathStart(Map<String, List<LpseToken>> panlTokenMap) {
		StringBuilder sb = new StringBuilder();

		if (valueSeparator != null) {
			if (hasValuePrefix) {
				sb.append(URLHelper.encodeURIPath(valuePrefix));
			}
		}

		if (panlTokenMap.containsKey(lpseCode)) {
			if (valueSeparator != null) {
				List<String> validValues = new ArrayList<>();

				for(LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
					if (lpseToken.getIsValid() && lpseToken.getValue() != null) {
						validValues.add(lpseToken.getValue());
					}
				}

				if (!validValues.isEmpty()) {
					for(String validValue : validValues) {
						sb.append(URLHelper.encodeURIPath(validValue));
						sb.append(URLHelper.encodeURIPath(valueSeparator));
					}
				}
			} else {
				for(LpseToken lpseToken : panlTokenMap.get(lpseCode)) {
					if (lpseToken.getIsValid()) {
						sb.append(getEncodedPanlValue(lpseToken));
						sb.append("/");
					}
				}
				return (sb.toString());
			}
		}

		return (sb.toString());
	}

	private String getMultiValueURIPathEnd() {
		StringBuilder sb = new StringBuilder();
		if (valueSeparator != null) {
			if (hasValueSuffix) {
				sb.append(URLHelper.encodeURIPath(valueSuffix));
			}
		}
		return (sb.toString());
	}

}
