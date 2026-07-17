import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Conferencia } from './conferencia';

describe('Conferencia', () => {
  let component: Conferencia;
  let fixture: ComponentFixture<Conferencia>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Conferencia],
    }).compileComponents();

    fixture = TestBed.createComponent(Conferencia);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
