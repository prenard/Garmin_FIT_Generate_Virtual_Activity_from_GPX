
public class Stacked_GPX_segment
{
	private int Start_trkpt_id;
	private int End_trkpt_id;
	private Double Start_Elevation;
	private Double End_Elevation;
	private Double Distance_Horizontal;
	
	public Stacked_GPX_segment(int s)
	{
		this.Start_trkpt_id = s;
		this.End_trkpt_id = 0;
	}

	public int getStart_trkpt_id()
	{
		return this.Start_trkpt_id;
	}

	public void setEnd_trkpt_id(int e)
	{
		this.End_trkpt_id = e;
	}
	public int getEnd_trkpt_id()
	{
		return this.End_trkpt_id;
	}

	public void setStart_Elevation(Double e)
	{
		this.Start_Elevation = e;
	}
	public Double getStart_Elevation()
	{
		return this.Start_Elevation;
	}
	public void setEnd_Elevation(Double e)
	{
		this.End_Elevation = e;
	}
	public Double getEnd_Elevation()
	{
		return this.End_Elevation;
	}

	public void setDistance_Horizontal(Double d)
	{
		this.Distance_Horizontal = d;
	}

	public Double getDistance_Horizontal()
	{
		return this.Distance_Horizontal;
	}
	public Double getGrade()
	{
		Double grade = (double) 0;
		if (Distance_Horizontal > 0)
		{
			grade = (this.End_Elevation - this.Start_Elevation) / this.Distance_Horizontal;
		}
		else
		{
			grade = (double) 0;
		}
		return grade;
	}
	public Double getDelta_Elevation()
	{
		return (this.End_Elevation - this.Start_Elevation);
	}

}
