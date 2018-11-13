public class Stacked_GPX_trkpt
{
	private Double Latitude;
	private Double Longitude;
	private Double Elevation;
	private Double Distance_Horizontal;
	
	public Stacked_GPX_trkpt(Double latitude,Double longitude,Double elevation)
	{
		this.Latitude = latitude;
		this.Longitude = longitude;
		this.Elevation = elevation;
		this.Distance_Horizontal = (double) 0;
	}

	public double getLatitude()
	{
		return this.Latitude;
	}
	public double getLongitude()
	{
		return this.Longitude;
	}
	public double getElevation()
	{
		return this.Elevation;
	}

	public void setDistance_Horizontal(Double d)
	{
		this.Distance_Horizontal = d;
	}

	public Double getDistance_Horizontal()
	{
		return this.Distance_Horizontal;
	}

}