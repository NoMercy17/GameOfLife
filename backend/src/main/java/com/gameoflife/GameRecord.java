package com.gameoflife;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="games")
public class GameRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="duration_seconds")
    private Integer durationSeconds;

    @Column(name = "total_cells")
    private Integer totalCells;

    @Column(name = "total_sexual_cells")
    private Integer totalSexualCells;

    @Column(name = "total_asexual_cells")
    private Integer totalAsexualCells;

    @Column(name = "alive_sexual_cells")
    private Integer aliveSexualCells;

    @Column(name = "alive_asexual_cells")
    private Integer aliveAsexualCells;

    @Column(name = "nr_divisions")
    private Integer nrDivisions;

    @Column(name = "nr_reproductions")
    private Integer nrReproductions;

    @Column(name="summary", length = 2000)
    private String summary;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public GameRecord() {
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getTotalCells() {
        return totalCells;
    }

    public void setTotalCells(Integer totalCells) {
        this.totalCells = totalCells;
    }

    public Integer getTotalSexualCells() {
        return totalSexualCells;
    }

    public void setTotalSexualCells(Integer totalSexualCells) {
        this.totalSexualCells = totalSexualCells;
    }

    public Integer getTotalAsexualCells() {
        return totalAsexualCells;
    }

    public void setTotalAsexualCells(Integer totalAsexualCells) {
        this.totalAsexualCells = totalAsexualCells;
    }

    public Integer getAliveSexualCells() {
        return aliveSexualCells;
    }

    public void setAliveSexualCells(Integer aliveSexualCells) {
        this.aliveSexualCells = aliveSexualCells;
    }

    public Integer getAliveAsexualCells() {
        return aliveAsexualCells;
    }

    public void setAliveAsexualCells(Integer aliveAsexualCells) {
        this.aliveAsexualCells = aliveAsexualCells;
    }

    public Integer getNrDivisions() {
        return nrDivisions;
    }

    public void setNrDivisions(Integer nrDivisions) {
        this.nrDivisions = nrDivisions;
    }

    public Integer getNrReproductions() {
        return nrReproductions;
    }

    public void setNrReproductions(Integer nrReproductions) {
        this.nrReproductions = nrReproductions;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format(
                "Duration: %d seconds, Total Cells: %d (Sexual: %d, Asexual: %d), " +
                        "Alive at End: %d (Sexual: %d, Asexual: %d), " +
                        "Divisions: %d, Reproductions: %d",
                durationSeconds, totalCells, totalSexualCells, totalAsexualCells,
                (aliveSexualCells + aliveAsexualCells), aliveSexualCells, aliveAsexualCells,
                nrDivisions, nrReproductions
        );
    }
}
