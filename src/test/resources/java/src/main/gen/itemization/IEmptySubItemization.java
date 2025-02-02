package itemization;

interface IEmptySubItemization<INNER> {

  /**
   * Add item
   *
   * @param item
   * @return Next state
   */
  <ITEM> itemization.SubItemization<INNER, ITEM> item(ITEM item);
}
