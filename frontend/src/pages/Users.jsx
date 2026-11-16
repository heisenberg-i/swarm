// PURPOSE: SWARM plant-app page: Users.
import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Typography, Card, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem,
  Grid, Chip, IconButton, CircularProgress, FormControl, InputLabel, Select,
  Checkbox, ListItemText, OutlinedInput, Alert, Divider,
} from '@mui/material';
import { Add, Block, CheckCircle, Delete, Edit } from '@mui/icons-material';
import { userAPI, plantAPI, sensorAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { SENSOR_LABELS } from '../utils/constants';

const ROLES = ['PLANT_ADMIN', 'OPERATOR'];
const emptyForm = { name: '', email: '', mobile: '', password: '', role: 'OPERATOR', plantIds: [], nodeIds: [] };

const normalizeIds = (ids) => (ids || []).map((id) => Number(id)).filter((id) => !Number.isNaN(id));

export default function Users({ embedded = false, filterPlantId }) {
  const navigate = useNavigate();
  const { isSuperAdmin, canManagePlants, user: currentUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [plants, setPlants] = useState([]);
  const [sensorNodes, setSensorNodes] = useState([]);
  const [dialogSensors, setDialogSensors] = useState([]);
  const [loadingSensors, setLoadingSensors] = useState(false);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editId, setEditId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');
  const [newPlantName, setNewPlantName] = useState('');
  const [addingPlant, setAddingPlant] = useState(false);

  const manageablePlants = useMemo(() => {
    if (isSuperAdmin) return plants;
    const myPlantIds = normalizeIds(currentUser?.plantIds);
    return plants.filter((p) => myPlantIds.includes(Number(p.plantId)));
  }, [plants, isSuperAdmin, currentUser]);

  const toggleDeviceNodes = (nodes) => {
    const ids = nodes.map((n) => Number(n.nodeId));
    const current = new Set(normalizeIds(form.nodeIds));
    const allSelected = ids.every((id) => current.has(id));
    if (allSelected) {
      ids.forEach((id) => current.delete(id));
    } else {
      ids.forEach((id) => current.add(id));
    }
    setForm({ ...form, nodeIds: [...current] });
  };

  const availableSensors = useMemo(() => {
    const plantIds = normalizeIds(form.plantIds);
    if (!plantIds.length) return [];
    const source = dialogSensors.length > 0 ? dialogSensors : sensorNodes;
    return source.filter((n) => plantIds.includes(Number(n.plantId)));
  }, [sensorNodes, dialogSensors, form.plantIds]);

  const sensorDevices = useMemo(() => {
    const groups = new Map();
    availableSensors.forEach((n) => {
      const chip = n.deviceChipId || `manual-${n.nodeId}`;
      if (!groups.has(chip)) {
        const baseName = (n.nodeName || '').replace(/ (Temperature|Humidity|Gas|Methane)$/, '').trim();
        groups.set(chip, {
          chipId: chip,
          label: n.deviceChipId ? (baseName || `ESP-${chip.slice(-4)}`) : n.nodeName,
          nodes: [],
        });
      }
      groups.get(chip).nodes.push(n);
    });
    return [...groups.values()];
  }, [availableSensors]);

  const loadSensorsForPlants = async (plantIds) => {
    const ids = normalizeIds(plantIds);
    if (!ids.length) {
      setDialogSensors([]);
      return;
    }
    setLoadingSensors(true);
    try {
      const results = await Promise.all(ids.map((id) => sensorAPI.getByPlant(id)));
      const merged = results.flatMap((r) => r.data.data || []);
      const unique = [...new Map(merged.map((n) => [n.nodeId, n])).values()];
      setDialogSensors(unique);
    } catch {
      setDialogSensors([]);
    } finally {
      setLoadingSensors(false);
    }
  };

  const loadUsers = () => {
    setLoading(true);
    userAPI.getAll().then(({ data }) => {
      let list = data.data || [];
      if (filterPlantId) {
        list = list.filter((u) => (u.plantIds || []).map(Number).includes(Number(filterPlantId)));
      }
      setUsers(list);
      setError('');
    }).catch(() => setError('Unable to load users.')).finally(() => setLoading(false));
  };

  useEffect(() => {
    loadUsers();
    plantAPI.getAll().then(({ data }) => setPlants(data.data || []));
    sensorAPI.getAll().then(({ data }) => setSensorNodes(data.data || []));
  }, []);

  const openCreate = () => {
    setEditId(null);
    setForm(emptyForm);
    setDialogSensors([]);
    setError('');
    setDialogOpen(true);
  };

  const openEdit = (u) => {
    const plantIds = normalizeIds(u.plantIds);
    const nodeIds = normalizeIds(u.nodeIds);
    setEditId(u.id);
    setForm({
      name: u.name || '',
      email: u.email || '',
      mobile: u.mobile || '',
      password: '',
      role: u.role || 'OPERATOR',
      plantIds,
      nodeIds,
    });
    setError('');
    setDialogOpen(true);
    loadSensorsForPlants(plantIds);
  };

  const handlePlantChange = async (event) => {
    const plantIds = normalizeIds(event.target.value);
    const nodeIds = normalizeIds(form.nodeIds).filter((id) => {
      const node = [...dialogSensors, ...sensorNodes].find((n) => Number(n.nodeId) === id);
      return node && plantIds.includes(Number(node.plantId));
    });
    setForm({ ...form, plantIds, nodeIds });
    await loadSensorsForPlants(plantIds);
  };

  const handleAddPlant = async () => {
    if (!newPlantName.trim()) return;
    setAddingPlant(true);
    setError('');
    try {
      const { data } = await plantAPI.create({
        plantName: newPlantName.trim(),
        plantType: 'BIOGAS',
        status: 'ACTIVE',
      });
      const created = data.data;
      const newPlantIds = [...normalizeIds(form.plantIds), Number(created.plantId)];
      setPlants((prev) => [...prev, created]);
      setForm((prev) => ({
        ...prev,
        plantIds: newPlantIds,
      }));
      setNewPlantName('');
      await loadSensorsForPlants(newPlantIds);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create plant');
    } finally {
      setAddingPlant(false);
    }
  };

  const handleSave = async () => {
    setError('');
    try {
      const payload = {
        ...form,
        plantIds: normalizeIds(form.plantIds),
        nodeIds: normalizeIds(form.nodeIds),
        role: isSuperAdmin ? form.role : 'OPERATOR',
      };
      if (editId) {
        if (!payload.password) delete payload.password;
        await userAPI.update(editId, payload);
      } else {
        await userAPI.create(payload);
      }
      setDialogOpen(false);
      loadUsers();
      sensorAPI.getAll().then(({ data }) => setSensorNodes(data.data || []));
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save user');
    }
  };

  const plantName = (id) => plants.find((p) => p.plantId === id)?.plantName || id;

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h5" fontWeight={700}>User Management</Typography>
          <Typography variant="body2" color="text.secondary">
            Assign plants and field devices that each user can access. Commission hardware under Field Devices.
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<Add />} onClick={openCreate}>
          Add User
        </Button>
      </Box>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Plants</TableCell>
                <TableCell>Sensors</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    No plant users yet. Super Admin can add Plant Admins and Operators here.
                  </TableCell>
                </TableRow>
              ) : users.map((u) => (
                <TableRow key={u.id}>
                  <TableCell>{u.name}</TableCell>
                  <TableCell>{u.email}</TableCell>
                  <TableCell><Chip label={u.role?.replace('_', ' ')} size="small" /></TableCell>
                  <TableCell>
                    {(u.plantIds || []).length === 0 ? (
                      <Typography variant="caption" color="text.secondary">None</Typography>
                    ) : (
                      (u.plantIds || []).map((id) => (
                        <Chip key={id} label={plantName(id)} size="small" sx={{ mr: 0.5, mb: 0.5 }} />
                      ))
                    )}
                  </TableCell>
                  <TableCell>
                    <Chip label={`${(u.nodeIds || []).length} assigned`} size="small" variant="outlined" />
                  </TableCell>
                  <TableCell>
                    <Chip label={u.status} size="small" color={u.status === 'ACTIVE' ? 'success' : 'default'} />
                  </TableCell>
                  <TableCell>
                    <IconButton size="small" onClick={() => openEdit(u)} title="Edit user"><Edit /></IconButton>
                    {u.status === 'ACTIVE' ? (
                      <IconButton size="small" color="warning" onClick={() => userAPI.disable(u.id).then(loadUsers)} title="Disable">
                        <Block />
                      </IconButton>
                    ) : (
                      <IconButton size="small" color="success" onClick={() => userAPI.enable(u.id).then(loadUsers)} title="Enable">
                        <CheckCircle />
                      </IconButton>
                    )}
                    {isSuperAdmin && u.role !== 'SUPER_ADMIN' && (
                      <IconButton size="small" color="error" onClick={() => userAPI.delete(u.id).then(loadUsers)} title="Delete">
                        <Delete />
                      </IconButton>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>{editId ? 'Edit User' : 'Add User'}</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Mobile" value={form.mobile} onChange={(e) => setForm({ ...form, mobile: e.target.value })} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth label="Password" type="password" value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                required={!editId}
                helperText={editId ? 'Leave blank to keep current password' : 'Minimum 6 characters'}
              />
            </Grid>
            {isSuperAdmin && (
              <Grid item xs={12} sm={6}>
                <TextField fullWidth select label="Role" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                  {ROLES.map((r) => <MenuItem key={r} value={r}>{r.replace('_', ' ')}</MenuItem>)}
                </TextField>
              </Grid>
            )}

            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Assigned Plants</InputLabel>
                <Select
                  multiple
                  value={normalizeIds(form.plantIds)}
                  onChange={handlePlantChange}
                  input={<OutlinedInput label="Assigned Plants" />}
                  renderValue={(selected) => selected.map((id) => plantName(id)).join(', ')}
                >
                  {manageablePlants.map((p) => (
                    <MenuItem key={p.plantId} value={Number(p.plantId)}>
                      <Checkbox checked={normalizeIds(form.plantIds).includes(Number(p.plantId))} />
                      <ListItemText primary={p.plantName} secondary={p.location} />
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {(isSuperAdmin || canManagePlants) && (
              <Grid item xs={12}>
                <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-start' }}>
                  <TextField
                    fullWidth
                    label="Add new plant name"
                    value={newPlantName}
                    onChange={(e) => setNewPlantName(e.target.value)}
                    placeholder="e.g. Chennai Biogas Site"
                    helperText="Create a plant and assign it to this user"
                  />
                  <Button
                    variant="outlined"
                    onClick={handleAddPlant}
                    disabled={addingPlant || !newPlantName.trim()}
                    sx={{ mt: 1, minWidth: 110 }}
                  >
                    {addingPlant ? <CircularProgress size={20} /> : 'Add Plant'}
                  </Button>
                </Box>
              </Grid>
            )}

            {isSuperAdmin && (
              <Grid item xs={12}>
                <Divider sx={{ mb: 1 }} />
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
                  Pair SWARM hubs and add P&amp;ID instruments under Field Devices. Then assign those tags here.
                </Typography>
                <Button
                  size="small"
                  variant="outlined"
                  onClick={() => { setDialogOpen(false); navigate('/sensors'); }}
                >
                  Open Field Devices
                </Button>
              </Grid>
            )}

            <Grid item xs={12}>
              {sensorDevices.length > 0 && (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 1 }}>
                  {sensorDevices.map((device) => {
                    const nodeIds = device.nodes.map((n) => Number(n.nodeId));
                    const selected = nodeIds.every((id) => normalizeIds(form.nodeIds).includes(id));
                    return (
                      <Chip
                        key={device.chipId}
                        label={`${device.label} (${device.nodes.length} sensors)`}
                        color={selected ? 'primary' : 'default'}
                        variant={selected ? 'filled' : 'outlined'}
                        onClick={() => toggleDeviceNodes(device.nodes)}
                        sx={{ cursor: 'pointer' }}
                      />
                    );
                  })}
                </Box>
              )}
              <FormControl fullWidth disabled={!normalizeIds(form.plantIds).length || loadingSensors}>
                <InputLabel>Assigned Sensors</InputLabel>
                <Select
                  multiple
                  value={normalizeIds(form.nodeIds)}
                  onChange={(e) => setForm({ ...form, nodeIds: normalizeIds(e.target.value) })}
                  input={<OutlinedInput label="Assigned Sensors" />}
                  renderValue={(selected) => `${selected.length} sensor(s) selected`}
                >
                  {loadingSensors ? (
                    <MenuItem disabled>
                      <CircularProgress size={20} sx={{ mr: 1 }} /> Loading sensors...
                    </MenuItem>
                  ) : availableSensors.length === 0 ? (
                    <MenuItem disabled>No sensors for selected plants</MenuItem>
                  ) : (
                    sensorDevices.map((device) => [
                      <MenuItem key={`header-${device.chipId}`} disabled sx={{ opacity: 1, fontWeight: 600 }}>
                        {device.label}{device.chipId && !device.chipId.startsWith('manual-') ? ` · ${device.chipId}` : ''}
                      </MenuItem>,
                      ...device.nodes.map((n) => (
                        <MenuItem key={n.nodeId} value={Number(n.nodeId)} sx={{ pl: 4 }}>
                          <Checkbox checked={normalizeIds(form.nodeIds).includes(Number(n.nodeId))} />
                          <ListItemText
                            primary={n.nodeName}
                            secondary={`${plantName(n.plantId)} · ${SENSOR_LABELS[n.sensorType] || n.sensorType}`}
                          />
                        </MenuItem>
                      )),
                    ])
                  )}
                </Select>
              </FormControl>
              {!normalizeIds(form.plantIds).length ? (
                <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
                  Select at least one plant to assign sensors.
                </Typography>
              ) : !loadingSensors && availableSensors.length === 0 ? (
                <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
                  {isSuperAdmin
                    ? 'No field devices on this plant yet. Pair a SWARM hub or add a transmitter under Field Devices, then assign it here.'
                    : 'No field devices on the selected plants yet. Ask Super Admin to commission hardware under Field Devices.'}
                </Typography>
              ) : null}
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSave}>Save</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
