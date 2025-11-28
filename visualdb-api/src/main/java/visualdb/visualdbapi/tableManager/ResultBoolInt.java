package visualdb.visualdbapi.tableManager;

public class ResultBoolInt {

  private final boolean boolREsult;
  private final int idResult;

  public ResultBoolInt(boolean bool, int id) {
    this.boolREsult = bool;
    this.idResult = id;
  }

  public int getId() {
    return this.idResult;
  }

  public boolean getBoolResult() {
    return this.boolREsult;
  }
}
