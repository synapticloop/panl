package com.synapticloop.panl.server.handler.fielderiser.field;

import com.synapticloop.panl.exception.PanlServerException;
import com.synapticloop.panl.server.handler.tokeniser.token.LpseToken;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * <p>The abstract BasePrefixSuffixField is the parent object for any
 * parameter, operand, or facet that allows a prefix and/or suffix.</p>
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

		return (URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8));
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
}
