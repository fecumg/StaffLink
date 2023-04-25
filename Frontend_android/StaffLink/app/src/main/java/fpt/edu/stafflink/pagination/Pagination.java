package fpt.edu.stafflink.pagination;

import java.util.HashMap;

/**
 * @author Truong Duc Duong
 */

public class Pagination extends HashMap<String, String> {
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";

    private int pageNumber = 0;
    private int pageSize = 10;
    private String sortBy = "id";
    private String direction = ASC;

    public Pagination(int pageNumber, int pageSize, String sortBy, String direction) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.direction = direction;
        this.generateMap();
    }

    public Pagination(int pageNumber, int pageSize, String sortBy) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.generateMap();
    }

    public Pagination(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.generateMap();
    }

    public Pagination(int pageNumber) {
        this.pageNumber = pageNumber;
        this.generateMap();
    }

    public Pagination() {
    }

    private void generateMap() {
        put("pageNumber", String.valueOf(this.pageNumber));
        put("pageSize", String.valueOf(this.pageSize));
        put("sortBy", this.sortBy);
        put("direction", this.direction);
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        this.generateMap();
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.generateMap();
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
        this.generateMap();
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
        this.generateMap();
    }
}
