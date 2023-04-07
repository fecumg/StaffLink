package fpt.edu.stafflink.pagination;

import org.springframework.util.LinkedMultiValueMap;

/**
 * @author Truong Duc Duong
 */

public class MultiValuePagination extends LinkedMultiValueMap<String, String> {
    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    private int pageNumber = 0;
    private int pageSize = 10;
    private String sortBy = "id";
    private String direction = ASC;

    public MultiValuePagination(int pageNumber, int pageSize, String sortBy, String direction) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.direction = direction;
        this.generateMap();
    }

    public MultiValuePagination(int pageNumber, int pageSize, String sortBy) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.generateMap();
    }

    public MultiValuePagination(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.generateMap();
    }

    public MultiValuePagination(int pageNumber) {
        this.pageNumber = pageNumber;
        this.generateMap();
    }

    public MultiValuePagination() {
    }

    private void generateMap() {
        add("pageNumber", String.valueOf(this.pageNumber));
        add("pageSize", String.valueOf(this.pageSize));
        add("sortBy", this.sortBy);
        add("direction", this.direction);
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
