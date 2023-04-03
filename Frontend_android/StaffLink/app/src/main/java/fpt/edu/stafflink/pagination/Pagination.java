package fpt.edu.stafflink.pagination;

/**
 * @author Truong Duc Duong
 */

public class Pagination {
    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    private int pageNumber = 1;
    private int pageSize = 10;
    private String sortBy = "id";
    private String direction = ASC;

    public Pagination(int pageNumber, int pageSize, String sortBy, String direction) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.direction = direction;
    }

    public Pagination(int pageNumber, int pageSize, String sortBy) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
    }

    public Pagination(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public Pagination(int pageNumber) {
        this.pageNumber = pageNumber;
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
