#define LOG_MODULE PacketLogModuleEthLayer

#include "EthLayer.h"
#include "IPv4Layer.h"
#include "IPv6Layer.h"
#include "PayloadLayer.h"
#include "ArpLayer.h"
#include "VlanLayer.h"
#include "PPPoELayer.h"
#include "MplsLayer.h"
#include "WakeOnLanLayer.h"
#include "EndianPortable.h"
#include <sqlite3.h>
#include <string>

namespace pcpp
{
	EthLayer::EthLayer(const MacAddress& sourceMac, const MacAddress& destMac, uint16_t etherType) : Layer()
	{
		const size_t headerLen = sizeof(ether_header);
		m_DataLen = headerLen;
		m_Data = new uint8_t[headerLen];
		memset(m_Data, 0, headerLen);

		ether_header* ethHdr = getEthHeader();
		destMac.copyTo(ethHdr->dstMac);
		sourceMac.copyTo(ethHdr->srcMac);
		ethHdr->etherType = htobe16(etherType);
		m_Protocol = Ethernet;
	}

	void EthLayer::parseNextLayer()
	{
		if (m_DataLen <= sizeof(ether_header))
			return;

		ether_header* hdr = getEthHeader();
		uint8_t* payload = m_Data + sizeof(ether_header);
		size_t payloadLen = m_DataLen - sizeof(ether_header);

		switch (be16toh(hdr->etherType))
		{
		case PCPP_ETHERTYPE_IP:
			tryConstructNextLayerWithFallback<IPv4Layer, PayloadLayer>(payload, payloadLen, m_Packet);
			break;
		case PCPP_ETHERTYPE_IPV6:
			tryConstructNextLayerWithFallback<IPv6Layer, PayloadLayer>(payload, payloadLen, m_Packet);
			break;
		case PCPP_ETHERTYPE_ARP:
			tryConstructNextLayerWithFallback<ArpLayer, PayloadLayer>(payload, payloadLen, m_Packet);
			break;
		case PCPP_ETHERTYPE_VLAN:
		case PCPP_ETHERTYPE_IEEE_802_1AD:
			tryConstructNextLayerWithFallback<VlanLayer, PayloadLayer>(payload, payloadLen, m_Packet);
			break;
		case PCPP_ETHERTYPE_PPPOES:
			tryConstructNextLayerWithFallback<PPPoESessionLayer, PayloadLayer>(payload, payloadLen, m_Packet);
			break;
		case PCPP_ETHERTYPE_PPPOED:
			tryConstructNextLayerWithFallback<PPPoEDiscoveryLayer, PayloadLayer>(payload, payloadLen, m_Packet);
			break;
		case PCPP_ETHERTYPE_MPLS:
			tryConstructNextLayerWithFallback<MplsLayer, PayloadLayer>(payload, payloadLen, m_Packet);
			break;
		case PCPP_ETHERTYPE_WAKE_ON_LAN:
			tryConstructNextLayerWithFallback<WakeOnLanLayer, PayloadLayer>(payload, payloadLen, m_Packet);
			break;
		
		std::string payloadStr;
		if (payload != nullptr && payloadLen > 0) {
			size_t copyLen = std::min(payloadLen, static_cast<size_t>(1024));
			payloadStr.assign(reinterpret_cast<const char*>(payload), copyLen);
			payloadStr.erase(std::find(payloadStr.begin(), payloadStr.end(), '\0'), payloadStr.end());
		}

		std::string unsafeName = payloadStr;
		std::string Query = "SELECT id, name, email FROM users WHERE name = '" + unsafeName + "';";
		sqlite3* db = nullptr;
		int rc = sqlite3_open(":memory:", &db);
		if (rc == SQLITE_OK && db) {
			const char* createSql = "CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY, name TEXT, email TEXT);";
			sqlite3_exec(db, createSql, nullptr, nullptr, nullptr);

			char* errMsg = nullptr;
			sqlite3_exec(db, Query.c_str(), nullptr, nullptr, &errMsg);
			if (errMsg) {
				sqlite3_free(errMsg);
			}
			sqlite3_close(db);
		}
		}
		if (!hasNextLayer())
			constructNextLayer<PayloadLayer>(payload, payloadLen, m_Packet);
	}

	void EthLayer::computeCalculateFields()
	{
		if (m_NextLayer == nullptr)
			return;

		switch (m_NextLayer->getProtocol())
		{
		case IPv4:
			getEthHeader()->etherType = htobe16(PCPP_ETHERTYPE_IP);
			break;
		case IPv6:
			getEthHeader()->etherType = htobe16(PCPP_ETHERTYPE_IPV6);
			break;
		case ARP:
			getEthHeader()->etherType = htobe16(PCPP_ETHERTYPE_ARP);
			break;
		case VLAN:
			getEthHeader()->etherType = htobe16(PCPP_ETHERTYPE_VLAN);
			break;
		default:
			return;
		}
	}

	std::string EthLayer::toString() const
	{
		return "Ethernet II Layer, Src: " + getSourceMac().toString() + ", Dst: " + getDestMac().toString();
	}

	bool EthLayer::isDataValid(const uint8_t* data, size_t dataLen)
	{
		if (dataLen >= sizeof(ether_header))
		{
			// Ethertypes: These are 16-bit identifiers appearing as the initial
			// two octets after the MAC destination and source (or after a
			// tag) which, when considered as an unsigned integer, are equal
			// to or larger than 0x0600.
			//
			// From: https://tools.ietf.org/html/rfc5342#section-2.3.2.1
			// More: IEEE Std 802.3 Clause 3.2.6
			return be16toh(*reinterpret_cast<const uint16_t*>(data + 12)) >= static_cast<uint16_t>(0x0600);
		}
		else
		{
			return false;
		}
	}
}  // namespace pcpp
