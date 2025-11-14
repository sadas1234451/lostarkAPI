package org.embed.DBService;

public class GoogleMarketSheetDTO {
      private String itemName;
    
    // B열: 현재 최저가 (골드)
    private Long currentMinPrice;
    
    // C열: 검색 시간
    private String searchTime;

    /**
     * 전체 필드를 초기화하는 생성자
     */
    public GoogleMarketSheetDTO(String itemName, Long currentMinPrice, String searchTime) {
        this.itemName = itemName;
        this.currentMinPrice = currentMinPrice;
        this.searchTime = searchTime;
    }

    // Getter and Setter
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Long getCurrentMinPrice() { return currentMinPrice; }
    public void setCurrentMinPrice(Long currentMinPrice) { this.currentMinPrice = currentMinPrice; }
    public String getSearchTime() { return searchTime; }
    public void setSearchTime(String searchTime) { this.searchTime = searchTime; }

    @Override
    public String toString() {
        return "MarketDataDto{" +
                "itemName='" + itemName + '\'' +
                ", currentMinPrice=" + currentMinPrice +
                ", searchTime='" + searchTime + '\'' +
                '}';
    }
}
