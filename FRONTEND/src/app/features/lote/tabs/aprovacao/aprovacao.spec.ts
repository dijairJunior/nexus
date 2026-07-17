import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Aprovacao } from './aprovacao';

describe('Aprovacao', () => {
  let component: Aprovacao;
  let fixture: ComponentFixture<Aprovacao>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Aprovacao],
    }).compileComponents();

    fixture = TestBed.createComponent(Aprovacao);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
