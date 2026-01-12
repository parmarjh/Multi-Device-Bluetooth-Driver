using Microsoft.AspNetCore.Mvc;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace MultiDeviceBT.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DevicesController : ControllerBase
    {
        private readonly IDriverInterface _driver;

        public DevicesController(IDriverInterface driver)
        {
            _driver = driver;
        }

        // GET: api/devices
        [HttpGet]
        public async Task<ActionResult<IEnumerable<DeviceInfo>>> GetConnectedDevices()
        {
            var devices = await _driver.GetActiveConnectionsAsync();
            return Ok(devices);
        }

        // POST: api/devices/connect
        [HttpPost("connect")]
        public async Task<IActionResult> ConnectDevice([FromBody] ConnectRequest request)
        {
            if (string.IsNullOrEmpty(request.Address))
                return BadRequest("Device address is required.");

            var success = await _driver.ConnectDeviceAsync(request.Address, request.Priority);
            if (success)
                return Ok(new { message = $"Successfully connected to {request.Address}" });
            
            return StatusCode(500, "Failed to connect to device.");
        }

        // DELETE: api/devices/disconnect/{address}
        [HttpDelete("disconnect/{address}")]
        public async Task<IActionResult> DisconnectDevice(string address)
        {
            var success = await _driver.DisconnectDeviceAsync(address);
            if (success)
                return Ok(new { message = $"Disconnected {address}" });

            return NotFound("Device not found or disconnection failed.");
        }

        // GET: api/devices/stats
        [HttpGet("stats")]
        public async Task<ActionResult<DriverStats>> GetStats()
        {
            var stats = await _driver.GetDriverStatsAsync();
            return Ok(stats);
        }

        // POST: api/devices/iot/control
        [HttpPost("iot/control")]
        public async Task<IActionResult> SendIoTCommand([FromBody] IoTCommandRequest request)
        {
            var success = await _driver.SendIoTCommandAsync(request.Address, request.CommandId, request.Payload);
            if (success)
                return Ok(new { message = "Command sent successfully" });

            return BadRequest("Failed to send command.");
        }
    }

    public class ConnectRequest
    {
        public string Address { get; set; }
        public int Priority { get; set; }
    }

    public class IoTCommandRequest
    {
        public string Address { get; set; }
        public byte CommandId { get; set; }
        public byte[] Payload { get; set; }
    }

    public class DeviceInfo
    {
        public string Address { get; set; }
        public string Name { get; set; }
        public string Status { get; set; }
        public int Priority { get; set; }
        public string Type { get; set; }
    }

    public class DriverStats
    {
        public int ActiveConnections { get; set; }
        public long BytesSent { get; set; }
        public long BytesReceived { get; set; }
        public int AICorrectionsApplied { get; set; }
    }

    public interface IDriverInterface
    {
        Task<IEnumerable<DeviceInfo>> GetActiveConnectionsAsync();
        Task<bool> ConnectDeviceAsync(string address, int priority);
        Task<bool> DisconnectDeviceAsync(string address);
        Task<DriverStats> GetDriverStatsAsync();
        Task<bool> SendIoTCommandAsync(string address, byte cmdId, byte[] payload);
    }
}
