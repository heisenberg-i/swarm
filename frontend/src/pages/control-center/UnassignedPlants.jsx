import { useEffect, useState } from 'react';
import {
  Box, Typography, Card, CardContent, Button, Table, TableBody, TableCell,
  TableHead, TableRow, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, CircularProgress, Alert, Grid,
} from '@mui/material';
import { Add } from '@mui/icons-material';
import { controlCenterAPI } from '../../services/api';

const emptyPlant = {
  plantName: '',
  plantType: 'BIOGAS',
  location: '',
  capacity: '',
  feedstockType: '',
};

export default function UnassignedPlants() {
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(emptyPlant);
  const [saving, setSaving] = useState(false);

  const load = () => {
    setLoading(true);
    controlCenterAPI.listUnassignedPlants()
      .then(({ data }) => setPlants(data.data || []))
      .catch((e) => setError(e.response?.data?.message || 'Failed to load plants'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const create = async () => {
    setSaving(true);
    setError('');
    try {
      await controlCenterAPI.createPlant({
        ...form,
        capacity: form.capacity ? Number(form.capacity) : null,
      });
      setOpen(false);
      setForm(emptyPlant);
      load();
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to create plant');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>Unassigned plants</Typography>
        <Button startIcon={<Add />} variant="contained" onClick={() => setOpen(true)}>New Plant</Button>
      </Box>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        These plants are not given to a company yet. Assign them from a company page.
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      <Card>
        <CardContent>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Location</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {plants.length === 0 && (
                <TableRow>
                  <TableCell colSpan={3}>No unassigned plants.</TableCell>
                </TableRow>
              )}
              {plants.map((p) => (
                <TableRow key={p.plantId}>
                  <TableCell>{p.plantName}</TableCell>
                  <TableCell>{p.plantType}</TableCell>
                  <TableCell>{p.location || '—'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Create plant</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField fullWidth label="Plant name" value={form.plantName} onChange={(e) => setForm({ ...form, plantName: e.target.value })} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth select label="Type" value={form.plantType} onChange={(e) => setForm({ ...form, plantType: e.target.value })}>
                <MenuItem value="BIOGAS">BIOGAS</MenuItem>
                <MenuItem value="BIO_CNG">BIO CNG</MenuItem>
                <MenuItem value="ORGANIC_WASTE">ORGANIC WASTE</MenuItem>
                <MenuItem value="WASTE_TO_ENERGY">WASTE TO ENERGY</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Location" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Capacity" type="number" value={form.capacity} onChange={(e) => setForm({ ...form, capacity: e.target.value })} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Feedstock" value={form.feedstockType} onChange={(e) => setForm({ ...form, feedstockType: e.target.value })} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" disabled={!form.plantName.trim() || saving} onClick={create}>Create</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
