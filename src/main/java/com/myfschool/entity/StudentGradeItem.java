package com.myfschool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "student_grade_items")
public class StudentGradeItem implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "student_grade_id", nullable = false)
    private Long studentGradeId;

    @NotBlank
    @Column(name = "grade_category", nullable = false, length = 100)
    private String gradeCategory;

    @NotBlank
    @Column(name = "grade_item", nullable = false, length = 100)
    private String gradeItem;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(length = 50)
    private String value;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentGradeId() {
        return studentGradeId;
    }

    public void setStudentGradeId(Long studentGradeId) {
        this.studentGradeId = studentGradeId;
    }

    public String getGradeCategory() {
        return gradeCategory;
    }

    public void setGradeCategory(String gradeCategory) {
        this.gradeCategory = gradeCategory;
    }

    public String getGradeItem() {
        return gradeItem;
    }

    public void setGradeItem(String gradeItem) {
        this.gradeItem = gradeItem;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
