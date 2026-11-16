# PURPOSE: Operator/developer documentation for SWARM.
# Raspberry Pi — Fresh Setup for SWARM (Windows + Cursor Remote SSH)

Use this guide when your Pi **already runs an old project** and you want to **wipe it** and deploy **SWARM** as the primary server.

You will connect to the Pi from **Cursor** using **Remote SSH** (Connect to Host) — no WinSCP needed. Cursor gives you a remote terminal and file editor on the Pi in one window.

**Repo:** `swarmnfi-svg/swarm_webapp`  
**Target role:** `SWARM_DEPLOYMENT_ROLE=primary`

---

## Before you start


| Item                | Notes                                                              |
| ------------------- | ------------------------------------------------------------------ |
| Raspberry Pi 4 or 5 | 4 GB+ RAM recommended                                              |
| USB SSD or microSD  | Will be fully erased                                               |
| Windows PC          | Same Wi-Fi/LAN as the Pi                                           |
| **64-bit Pi OS**    | Required — Cursor Remote SSH does **not** support 32-bit (`armhf`) |
| Old project data    | Back up anything you need **before** formatting                    |




### Software on Windows

1. [Raspberry Pi Imager](https://www.raspberrypi.com/software/) — flash/format OS
2. [Cursor](https://cursor.com/) — IDE with Remote SSH
3. OpenSSH client (built into Windows 10/11) — for SSH from PowerShell

Verify SSH on Windows:

```powershell
ssh -V
```

If missing: **Settings → Apps → Optional features → OpenSSH Client → Install**.

---



## Part 1 — Back up old project (optional)

If you need files from the old project, connect once before wiping:

**Option A — PowerShell (quick backup)**

```powershell
scp -r swarm@<OLD-PI-IP>:/home/swarm/old_project_folder C:\Users\seena\backup\
```

**Option B — Cursor terminal**

```powershell
ssh swarm@<OLD-PI-IP>
# on Pi:
mysqldump -u root -p your_old_db > ~/backup.sql
exit
scp swarm@<OLD-PI-IP>:~/backup.sql C:\Users\seena\backup\
```

**Skip this part** if you do not need anything from the old project.

---



## Part 2 — Format and re-flash the Pi (wipes everything)

This removes the old OS, old project, and all data on the boot drive.

### Step 1 — Power off the Pi

If you can still SSH in:

```bash
sudo shutdown -h now
```

Unplug power. Remove the USB SSD or microSD and connect it to your **Windows PC**.

### Step 2 — Flash with Raspberry Pi Imager

1. Open **Raspberry Pi Imager**.
2. **Choose device** → your Pi model (Pi 4 or Pi 5).
3. **Choose OS** → **Raspberry Pi OS (64-bit)** → **Raspberry Pi OS Lite** (recommended) or Desktop.
  - **Must be 64-bit** — Cursor Remote SSH requires `aarch64`.
4. Click the **gear icon** (Advanced options):


| Setting         | Recommended value                            |
| --------------- | -------------------------------------------- |
| Set hostname    | `swarm-pi`                                   |
| Enable SSH      | **Yes**                                      |
| Authentication  | Swarm1234                                    |
| Set username    | `swarm` (or `pi`)                            |
| Set password    | Strong password (save it)                    |
| Configure Wi-Fi | Your SSID + password (if not using Ethernet) |
| Set locale      | Your timezone                                |


1. Click **Save**.
2. **Choose storage** → select the Pi’s SSD or microSD.
3. Click **Write** → confirm **Yes** (this **formats and erases** the drive).
4. Wait until finished → **Safely Eject** from Windows.



### Step 3 — Boot the Pi

1. Insert SSD/SD back into the Pi.
2. Connect **Ethernet** (recommended for first setup).
3. Power on and wait **3–5 minutes**.

---



## Part 3 — Find the Pi IP address



### Option A — Router admin page

1. Open router admin (e.g. `192.168.29.1`).
2. Find device `swarm-pi`.
3. Note the IP, e.g. `192.168.29.50`.



### Option B — Windows PowerShell

```powershell
ping swarm-pi.local
```



### Option C — Monitor + keyboard

```bash
hostname -I
uname -m    # must show aarch64 (64-bit)
```

Write down the IP: `________________`

---



## Part 4 — Connect to Pi from Cursor (Remote SSH)



### Step 1 — Test SSH from PowerShell first

```powershell
ssh swarm@192.168.29.50
```

Replace IP and username. Type `yes` for host key, enter password. If this works, Cursor will work too.

Type `exit` to disconnect.

### Step 2 — Create SSH config on Windows

Create or edit: `C:\Users\seena\.ssh\config`

```text
Host swarm-pi
    HostName 192.168.29.50
    User swarm
    Port 22
```

Change `HostName` and `User` to match your Pi.

### Step 3 — Connect in Cursor

1. Open **Cursor** on Windows.
2. Press `Ctrl+Shift+P` → type `Remote-SSH: Connect to Host` → Enter.
3. Select `swarm-pi` (from your SSH config).
4. Enter your Pi password when prompted.
5. Wait for Cursor to install the remote server on the Pi (first time only — 1–2 minutes).

When connected, the bottom-left corner of Cursor shows `SSH: swarm-pi`.

### Step 4 — Open terminal on the Pi

In Cursor: **Terminal → New Terminal** (or `Ctrl+``).

The prompt should look like:

```text
swarm@swarm-pi:~$
```

All commands in Parts 5–8 run in this terminal.

### Step 5 — Open the project folder on the Pi

After cloning (Part 5), open the remote folder:

1. **File → Open Folder**
2. Navigate to `/home/swarm/swarm_webapp`
3. Click **OK**

You can now edit `.env.rpi`, docs, and configs directly in Cursor — changes save on the Pi.

---



## Part 5 — Prepare the Pi for SWARM

Run these in the **Cursor terminal** (connected to Pi).

### Step 1 — Update system

```bash
sudo apt update && sudo apt upgrade -y
```



### Step 2 — Install Docker and tools

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
sudo apt install -y docker-compose-plugin git mysql-client
```

**Reconnect Cursor SSH** so the docker group takes effect:

1. `Ctrl+Shift+P` → `Remote-SSH: Close Remote Connection`
2. Connect again to `swarm-pi`
3. Open terminal and verify:

```bash
docker --version
docker compose version
uname -m    # should print aarch64
```



### Step 3 — Create data directories

**Separate USB SSD for MySQL (recommended):**

```bash
lsblk
sudo mkdir -p /mnt/ssd/swarm/mysql /mnt/ssd/swarm/backups
sudo chown -R 999:999 /mnt/ssd/swarm/mysql
```

**Boot drive only (for now):**

```bash
mkdir -p ~/swarm_data/mysql ~/swarm_data/backups
sudo chown -R 999:999 ~/swarm_data/mysql
```



### Step 4 — Clone SWARM

```bash
cd ~
git clone https://github.com/swarmnfi-svg/swarm_webapp.git
cd swarm_webapp
```

Then in Cursor: **File → Open Folder** → `/home/swarm/swarm_webapp`.

### Step 5 — Create environment file

In Cursor terminal:

```bash
cp .env.rpi.example .env.rpi
```

Edit `.env.rpi` in Cursor (click the file in the sidebar).

**Minimum values to set:**

```env
# Public API URL (Cloudflare tunnel or LAN URL for testing)
SWARM_PUBLIC_API_URL=https://api.swarm.co.in/api
VITE_API_URL=https://api.swarm.co.in/api

# Must match Railway standby for failover
JWT_SECRET=<same JWT_SECRET as Railway backend>

# Database passwords — change these
DB_PASSWORD=your-strong-db-password
MYSQL_ROOT_PASSWORD=your-strong-root-password

# MySQL data path
SWARM_MYSQL_DATA_DIR=/mnt/ssd/swarm/mysql
# OR if no separate SSD:
# SWARM_MYSQL_DATA_DIR=/home/swarm/swarm_data/mysql

# Railway sync (fill after Railway MySQL TCP proxy is enabled)
RAILWAY_MYSQL_HOST=
RAILWAY_MYSQL_PORT=
RAILWAY_MYSQL_USER=root
RAILWAY_MYSQL_PASSWORD=
RAILWAY_MYSQL_DATABASE=railway
SYNC_BACKUP_DIR=/mnt/ssd/swarm/backups
```

Save with `Ctrl+S`. The file is saved directly on the Pi.

---



## Part 6 — Start SWARM

In Cursor terminal:

```bash
cd ~/swarm_webapp
docker compose -f docker-compose.rpi.yml --env-file .env.rpi up -d --build
```

First build takes **15–30 minutes** on a Pi.

Watch logs:

```bash
docker compose -f docker-compose.rpi.yml logs -f
```

Press `Ctrl+C` to stop watching.

### Health check

On the Pi (Cursor terminal):

```bash
curl http://localhost:8080/api/health
```

From your Windows browser:

```text
http://<PI-IP>:8080/api/health
http://<PI-IP>
```

---



## Part 7 — Set static IP (recommended)

So Cursor always connects to the same address.

**Easiest:** DHCP reservation in your router for the Pi’s MAC address.

**Or on the Pi** (Cursor terminal):

```bash
sudo nano /etc/dhcpcd.conf
```

Add at the bottom (change IPs for your network):

```text
interface eth0
static ip_address=192.168.29.50/24
static routers=192.168.29.1
static domain_name_servers=192.168.29.1
```

```bash
sudo reboot
```

Update `C:\Users\seena\.ssh\config` with the new IP, then reconnect in Cursor.

---



## Part 8 — Hourly sync to Railway (after Pi is running)

```bash
chmod +x ~/swarm_webapp/scripts/sync-to-railway.sh
cd ~/swarm_webapp
./scripts/sync-to-railway.sh --dry-run
./scripts/sync-to-railway.sh --dump-only
```

When Railway MySQL credentials are set in `.env.rpi`:

```bash
./scripts/sync-to-railway.sh
```

Add cron (hourly):

```bash
crontab -e
```

Add:

```cron
0 * * * * cd /home/swarm/swarm_webapp && ./scripts/sync-to-railway.sh >> /var/log/swarm-sync.log 2>&1
```

(Change `/home/swarm` if your username is different.)

---



## Part 9 — Cloudflare and Railway (manual)


| Task                     | Where                     | Doc                                              |
| ------------------------ | ------------------------- | ------------------------------------------------ |
| Railway standby env vars | Railway dashboard         | [RAILWAY_HOSTING.md](RAILWAY_HOSTING.md)         |
| Cloudflare Tunnel on Pi  | Cloudflare Zero Trust     | [RPi_PRIMARY_RUNBOOK.md](RPi_PRIMARY_RUNBOOK.md) |
| Failover health checks   | Cloudflare Load Balancing | [CLOUDFLARE_FAILOVER.md](CLOUDFLARE_FAILOVER.md) |


**Railway backend must have:**

```env
SPRING_PROFILES_ACTIVE=prod
SWARM_DEPLOYMENT_ROLE=standby
JAVA_TOOL_OPTIONS=-XX:+UseContainerSupport -Xms128m -Xmx384m
```

---



## Checklist


| Step                                                     | Done |
| -------------------------------------------------------- | ---- |
| Old project backed up (if needed)                        | ☐    |
| Pi SSD/SD re-flashed with **64-bit** Raspberry Pi OS     | ☐    |
| SSH enabled; username/password set                       | ☐    |
| Pi IP address found; `uname -m` shows `aarch64`          | ☐    |
| `ssh swarm@<IP>` works from PowerShell                   | ☐    |
| SSH config created at `C:\Users\seena\.ssh\config`       | ☐    |
| Cursor connects via **Remote-SSH: Connect to Host**      | ☐    |
| Docker + docker compose installed                        | ☐    |
| `swarm_webapp` cloned; folder opened in Cursor           | ☐    |
| `.env.rpi` configured                                    | ☐    |
| `docker compose -f docker-compose.rpi.yml up -d --build` | ☐    |
| `http://<PI-IP>:8080/api/health` returns OK              | ☐    |
| Static IP or DHCP reservation set                        | ☐    |
| Railway standby env vars set                             | ☐    |
| Hourly sync cron added                                   | ☐    |
| Cloudflare tunnel + failover configured                  | ☐    |


---



## Troubleshooting


| Problem                                       | Solution                                                                |
| --------------------------------------------- | ----------------------------------------------------------------------- |
| Cursor “Failed to connect to remote SSH host” | Test `ssh swarm@<IP>` in PowerShell first; ensure SSH enabled in Imager |
| Cursor downloads `armhf` / 404 error          | Pi is 32-bit — re-flash with **Raspberry Pi OS (64-bit)**               |
| “Access denied”                               | Use username/password from Imager setup                                 |
| Cannot find Pi on network                     | Use Ethernet; check router device list                                  |
| `docker: permission denied`                   | `sudo usermod -aG docker $USER` → close and reconnect Cursor SSH        |
| Cursor remote server stuck                    | On Pi: `rm -rf ~/.cursor-server` then reconnect                         |
| Build fails / out of memory                   | Pi 4GB+; run `docker system prune -a`                                   |
| Port 8080 not reachable from PC               | `sudo ufw allow 8080` (if ufw enabled)                                  |
| ESP cannot reach API                          | `SWARM_PUBLIC_API_URL` must be public HTTPS, not `localhost`            |


---



## Daily workflow with Cursor


| Task             | How                                                           |
| ---------------- | ------------------------------------------------------------- |
| Connect to Pi    | `Ctrl+Shift+P` → **Remote-SSH: Connect to Host** → `swarm-pi` |
| Run commands     | Cursor integrated terminal (`Ctrl+``)                         |
| Edit config      | Open `/home/swarm/swarm_webapp/.env.rpi` in sidebar           |
| View Docker logs | Terminal: `docker compose -f docker-compose.rpi.yml logs -f`  |
| Disconnect       | `Ctrl+Shift+P` → **Remote-SSH: Close Remote Connection**      |


---



## Related docs

- [RPi_PRIMARY_RUNBOOK.md](RPi_PRIMARY_RUNBOOK.md) — full primary server operations
- [RAILWAY_HOSTING.md](RAILWAY_HOSTING.md) — Railway standby configuration
- [CLOUDFLARE_FAILOVER.md](CLOUDFLARE_FAILOVER.md) — health checks and failover

