import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Box, Typography, Card, CardContent, Button, Table, TableBody, TableCell,
  TableHead, TableRow, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, CircularProgress, Alert, Grid, Tabs, Tab, Chip,
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

export default function PlantInventory() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const tab = searchParams.get('tab') || 'all';
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(emptyPlant);
  const [saving, setSaving] = useState(false);

  const load = () => {
    setLoading(true);
    controlCenterAPI.listPlants()
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
      setSearchParams({ tab: 'unassigned' });
      load();
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to create plant');
    } finally {
      setSaving(false);
    }
  };

  const filtered = useMemo(() => {
    if (tab === 'unassigned') return plants.filter((p) => !p.companyId);
    if (tab === 'assigned') return plants.filter((p) => p.companyId);
    return plants;
  }, [plants, tab]);

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1, gap: 2, flexWrap: 'wrap' }}>
        <Box>
          <Typography variant="h5" fontWeight={700}>Plant inventory</Typography>
          <Typography variant="body2" color="text.secondary">
            Create sites in the unassigned pool, then attach them to a tenant. Live operations stay in the Swarm app.
          </Typography>
        </Box>
        <Button startIcon={<Add />} variant="contained" onClick={() => setOpen(true)}>New plant</Button>
      </Box>
      <Tabs value={tab} onChange={(_, v) => setSearchParams({ tab: v })} sx={{ mb: 2 }}>
        <Tab value="all" label={`All (${plants.length})`} />
        <Tab value="unassigned" label={`Unassigned (${plants.filter((p) => !p.companyId).length})`} />
        <Tab value="assigned" label={`Assigned (${plants.filter((p) => p.companyId).length})`} />
      </Tabs>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      <Card>
        <CardContent>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Plant</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Location</TableCell>
                <TableCell>Tenant</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.length === 0 && (
                <TableRow>
                  <TableCell colSpan={4}>No plants in this view. Create a plant, then assign it on the tenant page.</TableCell>
                </TableRow>
              )}
              {filtered.map((p) => (
                <TableRow
                  key={p.plantId}
                  hover
                  sx={{ cursor: p.companyId ? 'pointer' : 'default' }}
                  onClick={() => p.companyId && navigate(`/companies/${p.companyId}`)}
                >
                  <TableCell>{p.plantName}</TableCell>
                  <TableCell>{p.plantType}</TableCell>
                  <TableCell>{p.location || '—'}</TableCell>
                  <TableCell>
                    {p.companyName
                      ? <Chip size="small" label={p.companyName} />
                      : <Chip size="small" variant="outlined" label="Unassigned" />}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Create plant (unassigned)</DialogTitle>
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
