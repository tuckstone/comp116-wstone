require 'packetfu'

stream = PacketFu::Capture.new(:start => true, :iface => 'eth0', :promisc => true)

mastercard = /5\d{3}(\s|-)?\d{4}(\s|-)?\d{4}(\s|-)?\d{4}/
visa       = /4\d{3}(\s|-)?\d{4}(\s|-)?\d{4}(\s|-)?\d{4}/
discover   = /6011(\s|-)?\d{4}(\s|-)?\d{4}(\s|-)?\d{4}/
am_exp     = /3\d{3}(\s|-)?\d{6}(\s|-)?\d{5}/

num_events = 1

caught = false

while caught==false do
  stream.stream.each do |p|
    pkt = PacketFu::Packet.parse(p)
    if !pkt.is_ip? or (pkt.class.name != "PacketFu::TCPPacket" and pkt.class.name != "PacketFu::UDPPacket")
      next
    end
    if pkt.class.name == "PacketFu::UDPPacket"
      packet_body = pkt.payload
    else
      packet_body = pkt.payload
      flag_val = pkt.tcp_header.tcp_flags.to_i
#Null Scan ---------------------------
      if flag_val == 0 
        puts "#{num_events}. ALERT: NULL scan is detected from #{pkt.ip_header.ip_saddr} (#{pkt.proto.last})!"
        num_events += 1
#XMas tree scan ----------------------
      elsif (flag_val == 41)
        puts "#{num_events}. ALERT: XMAS tree scan is detected from #{pkt.ip_header.ip_saddr} (#{pkt.proto.last})!"
        num_events += 1
      end
    end

#things common to udp and tcp
#credit card leaked ------------------
    if (packet_body.match(mastercard) != nil or packet_body.match(visa) != nil or packet_body.match(discover) != nil or packet_body.match(am_exp) != nil)
      puts "#{num_events}. ALERT: Credit card leaked in the clear from #{pkt.ip_header.ip_saddr} (#{pkt.proto.last})!"
      num_events += 1
#Password leaked ---------------------
    elsif (packet_body.match("PASS") != nil)
      puts "#{num_events}. ALERT: Password Leakage from #{pkt.ip_header.ip_saddr} (#{pkt.proto.last})!"
      num_events += 1
#XSS ---------------------------------
    elsif (packet_body.match("<script>") != nil and (packet_body.match("GET") != nil or packet_body.match("POST") != nil))
      puts "#{num_events}. ALERT: XSS detected from #{pkt.ip_header.ip_saddr} (#{pkt.proto.last}))!"
      num_events += 1
#Other scans -------------------------
    elsif (packet_body.match("Nmap") != nil)
      puts "#{num_events}. ALERT: Nmap scan is detected from #{pkt.ip_header.ip_saddr} (#{pkt.proto.last})!"
      num_events += 1
    end
  end
end
