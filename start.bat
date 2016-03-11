public interface NumberGroup
{
    public boolean contains(int val);
}

public class Range implements NumberGroup
{
    private int min;
    private int max;
     
    public Range(int min, int max)
    { this.min=min; this.max = max;}

    public boolean contains(int val)
    { return val>=this.min && val<= this.max;}
}

//in MultipleGroups
public boolean contains(int val)
{
  for (NumberGroup ng : groupList)
    if (ng.contains(val)) return true;
  return false;
}