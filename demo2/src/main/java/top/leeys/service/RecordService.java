package top.leeys.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import top.leeys.domain.Record;
import top.leeys.dto.RankDTO;
import top.leeys.repository.CustomRepository;
import top.leeys.repository.RecordRepository;

@Service
public class RecordService {
	@Autowired RecordRepository recordRepository; 
	@Autowired CustomRepository customRepository;
	
	public Record save(Record record) {
	    return recordRepository.save(record);
	}
	
	public List<RankDTO> getRankingInfo() {
	    return customRepository.getRankInfo();
	}
	
}
