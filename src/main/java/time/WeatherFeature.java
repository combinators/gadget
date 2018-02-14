package time;

/**
 * Designated capability to retrieve weather using just a Zip code.
 *
 * Customized to also report weather values using designated TemperatureUnit.
 */
public class WeatherFeature extends FrequencyFeature {
    public final String location;
    public final String zip;
    public final TemperatureUnit temperatureUnit;

    public WeatherFeature() {
        this ("", "", TemperatureUnit.None,
                new FrequencyFeature(30, FrequencyUnit.Minute)); }

    public WeatherFeature(String location, String zipCode, TemperatureUnit temperatureUnit, FrequencyFeature freq) {
        super(freq.count, freq.unit);
        this.location = location;
        this.zip = zipCode;
        this.temperatureUnit = temperatureUnit;
    }


    @Override
    public FeatureUnit getUnit() { return FeatureUnit.Weather; }
}
