package org.ohmyopensource.ohmyuniversity.fetcher.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "mur_enrolled_stats")
public class MurEnrolledStats extends BaseEntity {

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "female_enrolled")
    private Integer femaleEnrolled;

    @Column(name = "male_enrolled")
    private Integer maleEnrolled;

    @Column(name = "total_enrolled")
    private Integer totalEnrolled;

    @Column(name = "data_source")
    private String dataSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataStatus status;

    //Costruttore vuoto
    public MurEnrolledStats() {}

    // --- GETTER ---

    public String getAcademicYear() {
        return academicYear;
    }

    public Integer getFemaleEnrolled() {
        return femaleEnrolled;
    }

    public Integer getMaleEnrolled() {
        return maleEnrolled;
    }

    public Integer getTotalEnrolled() {
        return totalEnrolled;
    }

    public String getDataSource() {
        return dataSource;
    }

    public DataStatus getStatus(){
        return status;
    }

    // --- SETTER ---
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public void setFemaleEnrolled(Integer femaleEnrolled) {
        this.femaleEnrolled = femaleEnrolled;
    }

    public void setMaleEnrolled(Integer maleEnrolled){
        this.maleEnrolled = maleEnrolled;
    }

    public void setTotalEnrolled(Integer totalEnrolled) {
        this.totalEnrolled = totalEnrolled;
    }

    public void setDataSource(String dataSource){
        this.dataSource = dataSource;
    }

    public void setStatus(DataStatus status){
        this.status = status;
    }

}
