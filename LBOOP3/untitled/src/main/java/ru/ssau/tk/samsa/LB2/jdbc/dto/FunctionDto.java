package ru.ssau.tk.samsa.LB2.jdbc.dto;

public class FunctionDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String type;

    public FunctionDto() {}

    public FunctionDto(Long id, Long ownerId, String name, String type) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.type = type;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
