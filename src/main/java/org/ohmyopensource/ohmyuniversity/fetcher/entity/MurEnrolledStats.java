package org.ohmyopensource.ohmyuniversity.fetcher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "mur_enrolled_stats")
public class MurEnrolledStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academic_year", nullable = false, unique = true, length = 9)
    private String academicYear;

    @Column(name = "enrolled_female", nullable = false)
    private Integer enrolledFemale;

    @Column(name = "enrolled_male", nullable = false)
    private Integer enrolledMale;

    @Column(name = "enrolled_total", nullable = false)
    private Integer enrolledTotal;

    @Column(name = "data_source", length = 255)
    private String dataSource;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    //Getters e setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAcademicYear() {
        return academicYear;
    }
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    public Integer getEnrolledFemale() {
        return enrolledFemale;
    }
    public void setEnrolledFemale(Integer enrolledFemale) {
        this.enrolledFemale = enrolledFemale;
    }
    public Integer getEnrolledMale() {
        return enrolledMale;
    }
    public void setEnrolledMale(Integer enrolledMale) {
        this.enrolledMale = enrolledMale;
    }
    public Integer getEnrolledTotal() {
        return enrolledTotal;
    }
    public void setEnrolledTotal(Integer enrolledTotal) {
        this.enrolledTotal = enrolledTotal;
    }
    public String getDataSource() {
        return dataSource;
    }
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    public String getNotes(){
        return notes;
    }
    public void setNotes(String notes){
        this.notes = notes;
    }
    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }
    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

}
