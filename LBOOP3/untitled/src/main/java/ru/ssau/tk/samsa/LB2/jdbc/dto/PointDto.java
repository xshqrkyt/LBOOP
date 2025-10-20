package ru.ssau.tk.samsa.LB2.jdbc.dto;

public class PointDto {
    private Long id;
    private Long functionId;
    private double x;
    private double y;
    private int index;

    public PointDto() {}

    public PointDto(Long id, Long functionId, double x, double y, int index) {
        this.id = id;
        this.functionId = functionId;
        this.x = x;
        this.y = y;
        this.index = index;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
}
