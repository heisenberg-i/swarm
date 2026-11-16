# PURPOSE: Plant HMI UI component used by the SWARM P&ID screens.
# Quarantined HMI React (SWARM 2.0 slice 0)

These components are **not** on the live Plant HMI path (`PlantHmi.jsx` + iframe `biogas-pfd.html`).

Live HMI: `BiogasPlantDiagram`, `OutputEnablePanel`, `HmiPageNav`, `PlantOverviewPanel`, `ZoneMonitorPanel`, `HmiAlarmSummary`, `HmiTrendsPanel`, `HmiDiagnosticsPanel`, `HmiAuditPanel`, `SwarmCoreHeader`.

Do not import this folder from product routes. A later slice may delete or revive a plant-specific P&ID pack after commissioning design.
