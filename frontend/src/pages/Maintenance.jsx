// PURPOSE: SWARM plant-app page: Maintenance.
import { useState, useEffect } from 'react';
import {
  Box, Typography, Card, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Chip, LinearProgress, FormControl, InputLabel, Select, MenuItem, CircularProgress, Alert,
} from '@mui/material';
import { plantAPI, maintenanceAPI } from '../services/api';
import { SENSOR_LABELS } from '../utils/constants';
import { pageHeaderRow, pageTitleSx, responsiveSelect } from '../utils/responsive';

export default function Maintenance({ fixedPlantId, embedded = false, hidePlantSelector = false }) {
  const [plants, setPlants] = useState([]);
  const [selectedPlant, setSelectedPlant] = useState(fixedPlantId || '');
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (fixedPlantId) {
      setSelectedPlant(Number(fixedPlantId));
      plantAPI.getById(fixedPlantId).then(({ data }) => setPlants([data.data])).catch(() => {});
      return;
    }
    plantAPI.getAll().then(({ data }) => {
      const list = data.data || [];
      setPlants(list);
      if (list.length > 0) setSelectedPlant(list[0].plantId);
    });
  }, [fixedPlantId]);

  useEffect(() => {
    if (!selectedPlant) return;
    setLoading(true);
    maintenanceAPI.getByPlant(selectedPlant)
      .then(({ data }) => setItems(data.data || []))
      .finally(() => setLoading(false));
  }, [selectedPlant]);

  const healthColor = (pct) => pct > 70 ? 'success' : pct > 40 ? 'warning' : 'error';

  return (
    <Box>
      <Box sx={pageHeaderRow}>
        {!embedded && <Typography variant="h5" sx={pageTitleSx}>Predictive Maintenance</Typography>}
        {!hidePlantSelector && (
          <FormControl size="small" sx={responsiveSelect}>
            <InputLabel>Plant</InputLabel>
            <Select value={selectedPlant} label="Plant" onChange={(e) => setSelectedPlant(e.target.value)}>
              {plants.map((p) => <MenuItem key={p.plantId} value={p.plantId}>{p.plantName}</MenuItem>)}
            </Select>
          </FormControl>
        )}
      </Box>

      <Card>
        {loading ? <Box sx={{ p: 4, textAlign: 'center' }}><CircularProgress /></Box> : (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Sensor</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Remaining Life (days)</TableCell>
                  <TableCell>Est. Failure Date</TableCell>
                  <TableCell>Health</TableCell>
                  <TableCell>Notes</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {items.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6}>
                      <Alert severity="info">
                        No sensors are paired on this plant yet. Pair a hub under Field Devices —
                        maintenance follows those live nodes, not generic pumps or blowers.
                      </Alert>
                    </TableCell>
                  </TableRow>
                )}
                {items.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>{item.equipmentName}</TableCell>
                    <TableCell>
                      <Chip
                        label={SENSOR_LABELS[item.sensorType] || item.sensorType || item.equipmentType}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{item.remainingUsefulLifeDays}</TableCell>
                    <TableCell>{item.estimatedFailureDate}</TableCell>
                    <TableCell sx={{ minWidth: 150 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <LinearProgress variant="determinate" value={item.healthPercentage || 0}
                          color={healthColor(item.healthPercentage)} sx={{ flexGrow: 1, height: 8, borderRadius: 4 }} />
                        <Typography variant="caption">{(item.healthPercentage || 0).toFixed(0)}%</Typography>
                      </Box>
                    </TableCell>
                    <TableCell sx={{ maxWidth: 250 }}>{item.notes}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Card>
    </Box>
  );
}
