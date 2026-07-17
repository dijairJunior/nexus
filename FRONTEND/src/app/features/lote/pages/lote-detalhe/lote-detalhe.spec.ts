import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoteDetalhe } from './lote-detalhe';

describe('LoteDetalhe', () => {
  let component: LoteDetalhe;
  let fixture: ComponentFixture<LoteDetalhe>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoteDetalhe],
    }).compileComponents();

    fixture = TestBed.createComponent(LoteDetalhe);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
