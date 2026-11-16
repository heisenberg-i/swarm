// PURPOSE: SWARM plant-app page: Reports.
import { useState, useEffect } from 'react';
import {
  Box, Typography, Card, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem, Grid, CircularProgress,
  FormControl, InputLabel, Select,
} from '@mui/material';
import { Add, Download } from '@mui/icons-material';
import { reportAPI, plantAPI } from '../services/api';
import { formatDate } from '../utils/constants';
import { pageHeaderRow, pageTitleSx, responsiveSelect } from '../utils/responsive';

const REPORT_TYPES = ['DAILY', 'WEEKLY', 'MONTHLY', 'PLANT_SUMMARY', 'GAS_PRODUCTION', 'ALERTS', 'PLANT_HEALTH'];
const FORMATS = ['PDF', 'EXCEL', 'CSV'];

export default function Reports({ fixedPlantId, embedded = false, hidePlantSelector = false }) {
  const [reports, setReports] = useState([]);
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedPlant, setSelectedPlant] = useState(fixedPlantId || '');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState({ plantId: fixedPlantId || '', reportType: 'DAILY', format: 'PDF' });

  const loadReports = (plantId) => {
    if (!plantId) {
      setReports([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    reportAPI.getAll(plantId).then(({ data }) => setReports(data.data || [])).finally(() => setLoading(false));
  };

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
    setForm((prev) => ({ ...prev, plantId: selectedPlant }));
    loadReports(selectedPlant);
  }, [selectedPlant]);

  const handleGenerate = async () => {
    if (!form.plantId) return;
    await reportAPI.generate({ ...form, plantId: Number(form.plantId) });
    setDialogOpen(false);
    loadReports(form.plantId);
  };

  return (
    <Box>
      <Box sx={pageHeaderRow}>
        {!embedded && <Typography variant="h5" sx={pageTitleSx}>Report Management</Typography>}
        <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' }, width: { xs: '100%', sm: 'auto' } }}>
          {!hidePlantSelector && (
            <FormControl size="small" sx={responsiveSelect}>
              <InputLabel>Plant</InputLabel>
              <Select value={selectedPlant} label="Plant" onChange={(e) => setSelectedPlant(e.target.value)}>
                {plants.map((p) => <MenuItem key={p.plantId} value={p.plantId}>{p.plantName}</MenuItem>)}
              </Select>
            </FormControl>
          )}
          <Button variant="contained" startIcon={<Add />} onClick={() => setDialogOpen(true)} disabled={!selectedPlant}>
            Generate Report
          </Button>
        </Box>
      </Box>

      <Card>
        {loading ? <CircularProgress /> : (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Title</TableCell><TableCell>Type</TableCell><TableCell>Format</TableCell>
                  <TableCell>Generated</TableCell><TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {reports.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell>{r.title}</TableCell>
                    <TableCell>{r.reportType}</TableCell>
                    <TableCell>{r.fileFormat}</TableCell>
                    <TableCell>{formatDate(r.createdAt)}</TableCell>
                    <TableCell>
                      <Button size="small" startIcon={<Download />}>Download</Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Card>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Generate Report</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField fullWidth select label="Plant" value={form.plantId} onChange={(e) => setForm({ ...form, plantId: e.target.value })}>
                {plants.map((p) => <MenuItem key={p.plantId} value={p.plantId}>{p.plantName}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth select label="Report Type" value={form.reportType} onChange={(e) => setForm({ ...form, reportType: e.target.value })}>
                {REPORT_TYPES.map((t) => <MenuItem key={t} value={t}>{t.replace(/_/g, ' ')}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth select label="Format" value={form.format} onChange={(e) => setForm({ ...form, format: e.target.value })}>
                {FORMATS.map((f) => <MenuItem key={f} value={f}>{f}</MenuItem>)}
              </TextField>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleGenerate} disabled={!form.plantId}>Generate</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
