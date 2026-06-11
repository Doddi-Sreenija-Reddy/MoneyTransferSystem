import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { TransferService } from './transfer';

describe('TransferService', () => {
  let service: TransferService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TransferService]
    });
    service = TestBed.inject(TransferService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
