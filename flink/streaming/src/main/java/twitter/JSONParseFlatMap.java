package twitter;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.sling.commons.json.JSONException;

public abstract class JSONParseFlatMap<IN, OUT> extends RichFlatMapFunction<IN, OUT> {

	private static final long serialVersionUID = 1L;

	// private static final Log LOG = LogFactory.getLog(JSONParseFlatMap.class);

	/**
	 * Get the value object associated with a key form a JSON code. It can find
	 * embedded fields, too.
	 * 
	 * @param jsonText
	 *            JSON String in which the field is searched.
	 * @param field
	 *            The key whose value is searched for.
	 * @return The object associated with the field.
	 * @throws JSONException
	 *             If the field is not found.
	 */
	public Object get(String jsonText, String field) throws JSONException {
		JSONParser parser = new JSONParser(jsonText);

		return parser.parse(field).get("retValue");
	}

	/**
	 * Get the boolean value associated with a key form a JSON code. It can find
	 * embedded fields, too.
	 * 
	 * @param jsonText
	 *            JSON String in which the field is searched.
	 * @param field
	 *            The key whose value is searched for.
	 * @return The object associated with the field.
	 * @throws JSONException
	 *             If the field is not found.
	 */
	public boolean getBoolean(String jsonText, String field) throws JSONException {
		JSONParser parser = new JSONParser(jsonText);

		return parser.parse(field).getBoolean("retValue");
	}

	/**
	 * Get the double value associated with a key form a JSON code. It can find
	 * embedded fields, too.
	 * 
	 * @param jsonText
	 *            JSON String in which the field is searched.
	 * @param field
	 *            The key whose value is searched for.
	 * @return The object associated with the field.
	 * @throws JSONException
	 *             If the field is not found.
	 */
	public double getDouble(String jsonText, String field) throws JSONException {
		JSONParser parser = new JSONParser(jsonText);

		return parser.parse(field).getDouble("retValue");
	}

	/**
	 * Get the int value associated with a key form a JSON code. It can find
	 * embedded fields, too.
	 * 
	 * @param jsonText
	 *            JSON String in which the field is searched.
	 * @param field
	 *            The key whose value is searched for.
	 * @return The object associated with the field.
	 * @throws JSONException
	 *             If the field is not found.
	 */
	public int getInt(String jsonText, String field) throws JSONException {
		JSONParser parser = new JSONParser(jsonText);

		return parser.parse(field).getInt("retValue");
	}

	/**
	 * Get the long value associated with a key form a JSON code. It can find
	 * embedded fields, too.
	 * 
	 * @param jsonText
	 *            JSON String in which the field is searched.
	 * @param field
	 *            The key whose value is searched for.
	 * @return The object associated with the field.
	 * @throws JSONException
	 *             If the field is not found.
	 */
	public long getLong(String jsonText, String field) throws JSONException {
		JSONParser parser = new JSONParser(jsonText);

		return parser.parse(field).getLong("retValue");
	}
	
	/**
	 * Get the String value associated with a key form a JSON code. It can find
	 * embedded fields, too.
	 * 
	 * @param jsonText
	 *            JSON String in which the field is searched.
	 * @param field
	 *            The key whose value is searched for.
	 * @return The object associated with the field.
	 * @throws JSONException
	 *             If the field is not found.
	 */
	public String getString(String jsonText, String field) throws JSONException {
		JSONParser parser = new JSONParser(jsonText);
		
		return parser.parse(field).getString("retValue");
}
}
