import { useEffect, useMemo, useState } from 'react';
import {
  Box, Typography, Card, CardContent, Table, TableBody, TableCell,
  TableHead, TableRow, Switch, CircularProgress, Alert, Tabs, Tab, Chip, FormControlLabel,
} from '@mui/material';
import { controlCenterAPI } from '../../services/api';

export default function HmiAccess() {
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [tab, setTab] = useState('all');
  const [savingId, setSavingId] = useState(null);

  const load = () => {
    setLoading(true);
    controlCenterAPI.listPlants()
      .then(({ data }) => setPlants(data.data || []))
      .catch((e) => setError(e.response?.data?.message || 'Failed to load plants'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const enabledCount = plants.filter((p) => p.hmiEnabled).length;
  const disabledCount = plants.length - enabledCount;

  const filtered = useMemo(() => {
    if (tab === 'enabled') return plants.filter((p) => p.hmiEnabled);
    if (tab === 'disabled') return plants.filter((p) => !p.hmiEnabled);
    return plants;
  }, [plants, tab]);

  const toggle = async (plant, enabled) => {
    setSavingId(plant.plantId);
    setError('');
    setSuccess('');
    try {
      const { data } = await controlCenterAPI.setPlantHmi(plant.plantId, enabled);
      const updated = data.data;
      setPlants((prev) => prev.map((p) => (p.plantId === plant.plantId ? { ...p, ...updated } : p)));
      setSuccess(enabled
        ? `HMI enabled for ${plant.plantName}. Tenant users can open Plant HMI.`
        : `HMI disabled for ${plant.plantName}. It is hidden in the Swarm app.`);
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to update HMI access');
    } finally {
      setSavingId(null);
    }
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Typography variant="h5" fontWeight={700}>Plant HMI access</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2, maxWidth: 760 }}>
        Enable HMI only for plants that should see the process diagram in the Swarm app.
        Until you enable it, Plant HMI is hidden for that tenant (sidebar, dashboard, and API).
        This page is <code>/hmi-access</code> so it does not clash with the process diagram files at <code>/hmi/</code>.
      </Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab value="all" label={`All (${plants.length})`} />
        <Tab value="enabled" label={`Enabled (${enabledCount})`} />
        <Tab value="disabled" label={`Disabled (${disabledCount})`} />
      </Tabs>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}
      <Card>
        <CardContent>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Plant</TableCell>
                <TableCell>Tenant</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>HMI</TableCell>
                <TableCell align="right">Enable / Disable</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5}>No plants in this view.</TableCell>
                </TableRow>
              )}
              {filtered.map((p) => (
                <TableRow key={p.plantId} hover>
                  <TableCell>{p.plantName}</TableCell>
                  <TableCell>
                    {p.companyName
                      ? <Chip size="small" label={p.companyName} />
                      : <Chip size="small" variant="outlined" label="Unassigned" />}
                  </TableCell>
                  <TableCell>{p.plantType}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={p.hmiEnabled ? 'Enabled' : 'Disabled'}
                      color={p.hmiEnabled ? 'success' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <FormControlLabel
                      control={
                        <Switch
                          checked={Boolean(p.hmiEnabled)}
                          disabled={savingId === p.plantId}
                          onChange={(e) => toggle(p, e.target.checked)}
                        />
                      }
                      label={p.hmiEnabled ? 'On' : 'Off'}
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </Box>
  );
}
